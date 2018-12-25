package network.piranya.platform.node.core.execution.liquidity;

import static network.piranya.platform.node.utilities.CollectionUtils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import network.piranya.platform.api.exceptions.InvalidParameterException;
import network.piranya.platform.api.exceptions.PiranyaException;
import network.piranya.platform.api.exceptions.SuitableLpNotFoundException;
import network.piranya.platform.api.extension_models.Input;
import network.piranya.platform.api.extension_models.InputMetadata;
import network.piranya.platform.api.extension_models.InputType;
import network.piranya.platform.api.extension_models.Parameters;
import network.piranya.platform.api.extension_models.execution.liquidity.LiquidityProvider;
import network.piranya.platform.api.extension_models.execution.liquidity.LiquidityProviderMetadata;
import network.piranya.platform.api.lang.None;
import network.piranya.platform.api.lang.Result;
import network.piranya.platform.api.models.metadata.LiquidityProviderInfo;
import network.piranya.platform.api.models.metadata.LiquidityProviderTypeInfo;
import network.piranya.platform.api.models.trading.Instrument;
import network.piranya.platform.node.api.execution.liquidity.LiquidityProvidersRegistry;
import network.piranya.platform.node.api.local_infrastructure.storage.KeyValueDb;
import network.piranya.platform.node.api.local_infrastructure.storage.LocalStorage;
import network.piranya.platform.node.core.execution.context.ManagerialExtensionContextImpl;
import network.piranya.platform.node.core.execution.engine.ExecutionEngine;
import network.piranya.platform.node.utilities.Encoder;
import network.piranya.platform.node.utilities.InputsValidator;
import network.piranya.platform.node.utilities.MetadataUtils;
import network.piranya.platform.node.utilities.ReflectionUtils;
import network.piranya.platform.node.utilities.StringUtils;

@SuppressWarnings("unchecked")
public class LiquidityProvidersRegistryImpl implements LiquidityProvidersRegistry {
	
	@Override
	public <LiquidityProviderType extends LiquidityProvider> void registerLiquidityProviderType(Class<LiquidityProviderType> liquidityProviderType) {
		if (!liquidityProviderType.isAnnotationPresent(LiquidityProviderMetadata.class)) {
			throw new PiranyaException(String.format("Liquidity Provider type '%s' must have LiquidityProviderMetadata annotation declared", liquidityProviderType.getName()));
		}
		
		LiquidityProviderMetadata metadata = liquidityProviderType.getAnnotation(LiquidityProviderMetadata.class);
		
		Input[] inputs = liquidityProviderType.isAnnotationPresent(InputMetadata.class) ? liquidityProviderType.getAnnotation(InputMetadata.class).inputs() : new Input[0];
		liquidityProvidersTypesMap().put(liquidityProviderType.getName(), new TypeRegistration((Class<LiquidityProvider>)liquidityProviderType,
				new LiquidityProviderTypeInfo(liquidityProviderType.getName(), metadata.displayName(), metadata.category(),
						metadata.description(), metadata.features(), metadata.searchTags(), MetadataUtils.getInputsInfo(inputs))));
	}
	
	@Override
	public <LiquidityProviderType extends LiquidityProvider> void deregisterLiquidityProviderType(Class<LiquidityProviderType> liquidityProviderType) {
		liquidityProvidersTypesMap().remove(liquidityProviderType.getName());
	}
	
	@Override
	public List<Class<LiquidityProvider>> liquidityProviderTypes() {
		return map((liquidityProvidersTypesMap().values()), m -> m.lpType());
	}
	
	@Override
	public List<LiquidityProviderTypeInfo> liquidityProviderTypesInfo() {
		return sort(map((liquidityProvidersTypesMap().values()), m -> m.info()));
	}
	
	@Override
	public LiquidityProviderTypeInfo liquidityProviderTypeInfo(String liquidityProviderType) {
		TypeRegistration reg = liquidityProvidersTypesMap().get(liquidityProviderType);
		if (reg == null) {
			throw new PiranyaException(String.format("Liquidity Provider Type '%s' is not registered", liquidityProviderType));
		}
		return reg.info;
	}
	
	@Override
	public LiquidityProvider liquidityProvider(String liquidityProviderId) {
		LiquidityProvider liquidityProvider = liquidityProvidersMap().get(liquidityProviderId);
		if (liquidityProvider == null) {
			throw new PiranyaException(String.format("Liquidity Provider '%s' is not registered", liquidityProviderId));
		}
		return liquidityProvider;
	}
	
	@Override
	public LiquidityProvider registerLiquidityProvider(String liquidityProviderTypeName, Parameters providerParams) {
		TypeRegistration reg = liquidityProvidersTypesMap().get(liquidityProviderTypeName);
		if (reg == null) {
			throw new PiranyaException(String.format("Liquidity Provider Type '%s' is not registered", liquidityProviderTypeName));
		}
		Class<LiquidityProvider> liquidityProviderType = reg.lpType();
		
		Input[] inputs = liquidityProviderType.isAnnotationPresent(InputMetadata.class) ? liquidityProviderType.getAnnotation(InputMetadata.class).inputs() : new Input[0];
		inputsValidator().validate(providerParams, inputs);
		
		LiquidityProvider liquidityProvider = ReflectionUtils.createInstance(liquidityProviderType);
		String lpid = providerParams.string("core:lp_id");
		if (StringUtils.isEmpty(lpid)) {
			throw new InvalidParameterException("Parameter '%s' must be set", "core:lp_id");
		} else if (liquidityProvidersMap().containsKey(lpid)) {
			throw new InvalidParameterException(String.format(
					"Liquidity Provider '%s' is already registered", lpid), "core:lp_id", false);
		}
		
		Consumer<Instrument> instrumentRegistrationConsumer = instrument -> registerInstrument(instrument, lpid);
		BiConsumer<Object, Map<String, Object>> stateUpdateConsumer = (customState, state) -> {
			if (!isLoading()) {
				storeLp(lpid, liquidityProviderTypeName, providerParams, customState, state);
			}
		};
		ReflectionUtils.inject(liquidityProvider, LiquidityProvider.class, "info", new LiquidityProviderInfo(
				lpid, liquidityProviderTypeName, providerParams.string("core:display_name", lpid),
				providerParams.string("core:description", ""), toPublicParamsMap(providerParams, inputs)));
		ReflectionUtils.inject(liquidityProvider, LiquidityProvider.class, "params", providerParams);
		ReflectionUtils.inject(liquidityProvider, LiquidityProvider.class, "context",
				new ManagerialExtensionContextImpl(executionEngineSupplier().get().createDetachedExtensionContext(moduleId(liquidityProviderType)), this));
		ReflectionUtils.inject(liquidityProvider, LiquidityProvider.class, "instrumentRegistrationConsumer", instrumentRegistrationConsumer);
		ReflectionUtils.inject(liquidityProvider, LiquidityProvider.class, "stateUpdateConsumer", stateUpdateConsumer);
		liquidityProvider.init();
		
		if (providerParams.bool("core:add_only_if_connected", false) && !isLoading()) {
			Result<None>[] connectionDone = new Result[1];
			final CountDownLatch latch = new CountDownLatch(1);
			liquidityProvider.connect(result -> {
				connectionDone[0] = result;
				latch.countDown();
			});
			try {
				latch.await(30, TimeUnit.SECONDS);
			} catch (InterruptedException ex) {
				throw new PiranyaException("Connection attempt timedout");
			}
			
			if (!connectionDone[0].isSuccessful()) {
				try { liquidityProvider.dispose(); } catch (Throwable ex) { }
				
				Exception error = connectionDone[0].error().get();
				LOG.warn(String.format("Failed to connect to LP '%s': %s", lpid, error.getMessage()), error);
				throw new PiranyaException("Error while connecting: " + error.getMessage());
			}
		} else if (!isLoading()) {
			storeLp(lpid, liquidityProviderTypeName, providerParams, null, null);
		}
		
		//providerParams = new ParametersBuilder().from(providerParams).bool("core:add_only_if_connected", false).build();
		
		liquidityProvidersMap().put(liquidityProvider.liquidityProviderId(), liquidityProvider);
		
		notifyListeners(new LpRegistrationEvent(liquidityProvider, LpRegistrationEvent.EventType.REGISTERED));
		
		return liquidityProvider;
	}
	
	protected Map<String, Object> toPublicParamsMap(Parameters params, Input[] inputs) {
		Map<String, Object> result = new HashMap<>();
		for (Input input : inputs) {
			if (input.type() != InputType.SECRET_STRING) {
				Object value = params.dataMap().get(input.id());
				if (value != null) {
					if (value instanceof List || value.getClass().isArray()) {
						result.put(input.id(), value);
					} else {
						result.put(input.id(), value.toString());
					}
				}
			}
		}
		return result;
	}
	
	protected String moduleId(Class<LiquidityProvider> liquidityProviderType) {
		// TODO for now not passed
		return null;
	}
	
	@Override
	public void deregisterLiquidityProvider(String liquidityProviderId) {
		LiquidityProvider liquidityProvider = liquidityProvidersMap().get(liquidityProviderId);
		if (liquidityProvider == null) {
			throw new PiranyaException(String.format("Liquidity Provider '%s' not found", liquidityProviderId));
		}
		
		// TODO deregister instruments
		
		liquidityProvidersMap().remove(liquidityProvider.liquidityProviderId());
		deleteLp(liquidityProviderId);
		liquidityProvider.dispose();
		
		notifyListeners(new LpRegistrationEvent(liquidityProvider, LpRegistrationEvent.EventType.DEREGISTERED));
	}
	
	@Override
	public List<LiquidityProvider> liquidityProviders() {
		return new ArrayList<>(liquidityProvidersMap().values());
	}
	
	@Override
	public void subscribe(Consumer<LpRegistrationEvent> listener) {
		liquidityProvidersListeners.put(listener, true);
	}
	
	@Override
	public void unsubscribe(Consumer<LpRegistrationEvent> listener) {
		liquidityProvidersListeners.remove(listener);
	}
	
	protected void notifyListeners(LpRegistrationEvent event) {
		for (Consumer<LpRegistrationEvent> listener : liquidityProvidersListeners.keySet()) {
			listener.accept(event);
		}
	}
	private final ConcurrentMap<Consumer<LpRegistrationEvent>, Boolean> liquidityProvidersListeners = new ConcurrentHashMap<>();
	
	@Override
	public LiquidityProvider liquidityProviderForInstrument(Instrument instrument) throws SuitableLpNotFoundException {
		List<LiquidityProvider> lps = liquidityProvidersForInstrument(instrument);
		if (lps.size() > 0) {
			return lps.get(0);
		} else {
			throw new SuitableLpNotFoundException(String.format("Suitable liquidity provider for instrument '%s' was not found", instrument));
		}
	}
	
	@Override
	public List<LiquidityProvider> liquidityProvidersForInstrument(Instrument instrument) {
		return null;
	}
	
	protected void registerInstrument(Instrument instrument, String lpid) {
		instrumentRegistrar().accept(instrument, lpid);
	}
	
	
	public void loadStoredLps() {
		try {
			setLoading(true);
			List<LpState> lpStates = listStoredLps();
			for (LpState lpState : lpStates) {
				LiquidityProvider lp = registerLiquidityProvider(lpState.liquidityProviderTypeName(), lpState.providerParams());
				ReflectionUtils.inject(lp, LiquidityProvider.class, "state", lpState.state());
				ReflectionUtils.inject(lp, LiquidityProvider.class, "customState", lpState.customState());
				
				System.out.println("state: " + lpState.state());
				// TODO
				if (lpState.state().containsKey("connect") && (Boolean)lpState.state().get("connect")) {
					lp.connect(result -> {});
				}
			}
		} finally {
			setLoading(false);
		}
	}
	
	protected void storeLp(String lpid, String liquidityProviderTypeName, Parameters providerParams, Object customState, Map<String, Object> state) {
		System.out.println("store: " + lpid + ": " + state);
		if (lpKvDb() != null) {
			lpKvDb().put(lpKvDb().byteBuffer(lpid.getBytes()), lpKvDb().byteBuffer(encoder().encode(new LpState(liquidityProviderTypeName, providerParams, customState, state))));
		}
	}
	
	protected void deleteLp(String lpid) {
		if (lpKvDb() != null) {
			lpKvDb().delete(lpKvDb().byteBuffer(lpid.getBytes()));
		}
	}
	
	protected List<LpState> listStoredLps() {
		List<LpState> result = new ArrayList<>();
		if (lpKvDb() != null) {
			lpKvDb().iterate(new byte[0], (key, value) -> result.add(encoder().decode(lpKvDb().bytesArray(value))));
		}
		return result;
	}
	
	private boolean loading = false;
	protected boolean isLoading() {
		return loading;
	}
	protected void setLoading(boolean loading) {
		this.loading = loading;
	}
	
	public static class LpState {
		
		private final String liquidityProviderTypeName;
		public String liquidityProviderTypeName() { return liquidityProviderTypeName; }
		
		private final Parameters providerParams;
		public Parameters providerParams() { return providerParams; }
		
		private final Object customState;
		public Object customState() { return customState; }
		
		private final Map<String, Object> state;
		public Map<String, Object> state() { return state; }
		
		public LpState(String liquidityProviderTypeName, Parameters providerParams, Object customState, Map<String, Object> state) {
			this.liquidityProviderTypeName = liquidityProviderTypeName;
			this.providerParams = providerParams;
			this.customState = customState;
			this.state = state;
		}
	}
	
	
	public LiquidityProvidersRegistryImpl(Supplier<ExecutionEngine> executionManagerSupplier, BiConsumer<Instrument, String> instrumentRegistrar, LocalStorage localStorage) {
		this.executionEngineSupplier = executionManagerSupplier;
		this.instrumentRegistrar = instrumentRegistrar;
		/// can be null in sandbox, etc.
		this.lpKvDb = localStorage != null ? localStorage.keyValueDb("lps-list", new KeyValueDb.Config(), this) : null;
		
		this.encoder.registerDataType(LpState.class, (short)0);
	}
	
	private final Supplier<ExecutionEngine> executionEngineSupplier;
	protected Supplier<ExecutionEngine> executionEngineSupplier() { return executionEngineSupplier; }
	
	private final BiConsumer<Instrument, String> instrumentRegistrar;
	protected BiConsumer<Instrument, String> instrumentRegistrar() { return instrumentRegistrar; }
	
	private final KeyValueDb lpKvDb;
	protected KeyValueDb lpKvDb() { return lpKvDb; }
	
	private final Encoder encoder = new Encoder();
	protected Encoder encoder() { return encoder; }
	
	private final ConcurrentMap<String, TypeRegistration> liquidityProvidersTypesMap = new ConcurrentHashMap<>();
	protected ConcurrentMap<String, TypeRegistration> liquidityProvidersTypesMap() { return liquidityProvidersTypesMap; }
	
	private final ConcurrentMap<String, LiquidityProvider> liquidityProvidersMap = new ConcurrentHashMap<>();
	protected ConcurrentMap<String, LiquidityProvider> liquidityProvidersMap() { return liquidityProvidersMap; }
	
	private final InputsValidator inputsValidator = new InputsValidator();
	protected InputsValidator inputsValidator() { return inputsValidator; }
	
	
	protected class TypeRegistration {
		
		private final Class<LiquidityProvider> lpType;
		public Class<LiquidityProvider> lpType() { return lpType; }
		
		private final LiquidityProviderTypeInfo info;
		public LiquidityProviderTypeInfo info() { return info; }
		
		public TypeRegistration(Class<LiquidityProvider> lpType, LiquidityProviderTypeInfo info) {
			this.lpType = lpType;
			this.info = info;
		}
	}
	
	
	private final static Logger LOG = LoggerFactory.getLogger(LiquidityProvidersRegistryImpl.class);
	
}
