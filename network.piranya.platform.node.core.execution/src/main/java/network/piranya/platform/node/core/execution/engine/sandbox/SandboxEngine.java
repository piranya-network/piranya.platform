package network.piranya.platform.node.core.execution.engine.sandbox;

import static network.piranya.platform.node.utilities.CollectionUtils.*;

import java.util.function.Consumer;

import network.piranya.platform.api.extension_models.execution.sandbox.SandboxConfiguration;
import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.api.models.info.MarketInfoProvider;
import network.piranya.platform.api.models.search.SearchService;
import network.piranya.platform.api.models.trading.ExecutionEvent;
import network.piranya.platform.node.api.booting.NetworkNodeConfig;
import network.piranya.platform.node.api.execution.bots.BotsRegistry;
import network.piranya.platform.node.api.execution.commands.CreateBot;
import network.piranya.platform.node.api.execution.liquidity.LiquidityProvidersRegistry;
import network.piranya.platform.node.api.local_infrastructure.LocalServices;
import network.piranya.platform.node.api.local_infrastructure.concurrency.Executor;
import network.piranya.platform.node.api.local_infrastructure.storage.LocalStorage;
import network.piranya.platform.node.api.local_infrastructure.storage.PersistentQueue;
import network.piranya.platform.node.core.execution.bots.BotsRegistryImpl;
import network.piranya.platform.node.core.execution.engine.ActivityLogger;
import network.piranya.platform.node.core.execution.engine.ExecutionEngine;
import network.piranya.platform.node.core.execution.engine.ExecutionTrail;
import network.piranya.platform.node.core.execution.engine.activity_log.EsLog;
import network.piranya.platform.node.core.execution.liquidity.LiquidityProvidersRegistryImpl;
import network.piranya.platform.node.utilities.CollectionUtils;

public class SandboxEngine extends ExecutionEngine {
	
	public void run() {
		executor().submit(this::doRun);
	}
	
	protected void doRun() {
		try {
			init();
			analyticsEngine().init();
			
			foreach(sandboxConfigurer().analyticalViews(), av -> {
				analyticsEngine().registerViewType(av.viewType);
				analyticsEngine().view(av.analyticalViewId, av.viewType.getName(), false, av.startPeriod, av.params, r -> {});
			});
			
			foreach(liquidityProvidersRegistry().liquidityProviders(), lp -> lp.connect(r -> {}));
			
			CollectionUtils.foreach(sandboxConfigurer().botsToRun(), b -> {
				if (b.isFeature()) {
					execute(CreateBot.createByFeature(b.botTypeId(), b.params(), Optional.empty(), r -> {}));
				} else {
					execute(CreateBot.createByType(b.botTypeId(), b.params(), Optional.empty(), r -> {}));
				}
			});
			
			/// 
			foreach(liquidityProvidersRegistry().liquidityProviders(), lp -> lp.connect(r -> {}));
		} catch (Throwable ex) {
			// TODO report and have a failure result
			ex.printStackTrace();
		}
	}
	
	public BotsRegistry getBotsRegistry() {
		return botsRegistry();
	}
	
	public void abort() {
		dispose();
	}
	
	
	@Override
	protected boolean acceptExecutionEvent(ExecutionEvent event) {
		boolean result = super.acceptExecutionEvent(event);
		if (find(sandboxConfigurer().finishConditions(), fc -> fc.eventType().isInstance(event) && fc.condition().apply(event)).isPresent()) {
			finishListener.ifPresent(l -> l.run());
		}
		return result;
	}
	
	
	public SandboxEngine(SandboxConfiguration configuration, NetworkNodeConfig config, LocalServices localServices,
			BotsRegistry currentBotsRegistry, LiquidityProvidersRegistry currentLiquidityProvidersRegistry, SearchService searchService, MarketInfoProvider marketInfoProvider) {
		// TODO replace storage. currently not a problem, can have potential ones
		super(config, localServices, new BotsRegistryImpl(localServices.localStorage()), null/*lp registry is overridden in this class*/,
				searchService, marketInfoProvider, new SandboxExecutionManager());
		
		((SandboxExecutionManager)executionManager()).setExecutionEngine(this);
		this.executor = localServices.executor();
		this.sandboxConfigurer = new SandboxConfigurer(currentBotsRegistry, currentLiquidityProvidersRegistry, botsRegistry(), liquidityProvidersRegistry());
		configuration.configure(sandboxConfigurer.configurationContext());
	}
	
	@Override
	protected ExecutionTrail newExecutionTrail() {
		return new ExecutionTrail(null) {
			@Override protected PersistentQueue initQueue(LocalStorage localStorage) { return null; }
			@Override public void append(EsLog log) { }
			@Override public void append(ExecutionEvent event) { }
			@Override public void replay(Consumer<EsLog> logAcceptor, Consumer<ExecutionEvent> eventAcceptor) { }
			@Override public void dispose() { }
		};
	}
	
	@Override
	protected ActivityLogger newActivityLogger() {
		return new DummyActivityLogger();
	}
	
	private final SandboxConfigurer sandboxConfigurer;
	protected SandboxConfigurer sandboxConfigurer() { return sandboxConfigurer; }
	
	private final Executor executor;
	protected Executor executor() { return executor; }

	public void onFinishListener(Runnable finishListener) {
		this.finishListener = Optional.of(finishListener);
	}
	private Optional<Runnable> finishListener;
	
	LiquidityProvidersRegistry liquidityProvidersRegistry = new LiquidityProvidersRegistryImpl(() -> this, (instrument, lpId) -> {}, null);
	@Override
	protected LiquidityProvidersRegistry liquidityProvidersRegistry() { return liquidityProvidersRegistry; }
	
}
