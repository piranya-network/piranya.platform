package network.piranya.platform.node.core.execution.context;

import static network.piranya.platform.node.utilities.CollectionUtils.map;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import network.piranya.platform.api.exceptions.SuitableLpNotFoundException;
import network.piranya.platform.api.extension_models.Parameters;
import network.piranya.platform.api.extension_models.execution.liquidity.LiquidityProvider;
import network.piranya.platform.api.models.metadata.LiquidityProviderTypeInfo;
import network.piranya.platform.api.models.trading.Instrument;
import network.piranya.platform.api.models.trading.liquidity.LiquidityProviderAdmin;
import network.piranya.platform.api.models.trading.liquidity.LiquidityProvidersAdmin;
import network.piranya.platform.node.api.execution.liquidity.LiquidityProvidersRegistry;
import network.piranya.platform.node.utilities.CollectionUtils;
import network.piranya.platform.node.utilities.EventsSubscriptionSupport;

public class LiquidityProvidersAdminImpl implements LiquidityProvidersAdmin {
	
	@Override
	public List<LiquidityProviderAdmin> liquidityProviders() {
		return map(registry().liquidityProviders(), this::lpAdmin);
	}
	
	@Override
	public LiquidityProviderAdmin liquidityProvider(String liquidityProviderId) {
		return lpAdmin(registry().liquidityProvider(liquidityProviderId));
	}
	
	@Override
	public List<LiquidityProviderTypeInfo> liquidityProvidersTypes() {
		return registry().liquidityProviderTypesInfo();
	}
	
	@Override
	public LiquidityProviderTypeInfo liquidityProviderType(String liquidityProviderTypeId) {
		return registry().liquidityProviderTypeInfo(liquidityProviderTypeId);
	}
	
	@Override
	public LiquidityProviderAdmin register(String liquidityProviderType, Parameters params) {
		return lpAdmin(registry().registerLiquidityProvider(liquidityProviderType, params));
	}
	
	@Override
	public void deregister(String liquidityProviderId) {
		registry().deregisterLiquidityProvider(liquidityProviderId);
	}
	
	@Override
	public void subscribe(Consumer<LiquidityProviderEvent> subscriber) {
		lpEventsSupport.subscribe(subscriber);
	}
	
	@Override
	public void unsubscribe(Consumer<LiquidityProviderEvent> subscriber) {
		lpEventsSupport.unsubscribe(subscriber);
	}
	
	@Override
	public LiquidityProviderAdmin liquidityProviderForInstrument(Instrument instrument) throws SuitableLpNotFoundException {
		return lpAdmin(registry().liquidityProviderForInstrument(instrument));
	}
	
	@Override
	public List<LiquidityProviderAdmin> liquidityProvidersForInstrument(Instrument instrument) {
		return map(registry().liquidityProvidersForInstrument(instrument), this::lpAdmin);
	}
	
	public void dispose() {
		registry().unsubscribe(this.lpRegistrationListener);
	}
	
	@Override
	protected void finalize() throws Throwable {
		dispose();
		
		CollectionUtils.foreach(lpAdmins.values(), lpAdmin -> lpAdmin.dispose());
	}
	
	
	protected LiquidityProviderAdminImpl lpAdmin(LiquidityProvider lp) {
		LiquidityProviderAdminImpl lpAdmin = lpAdmins.get(lp);
		if (lpAdmin == null) {
			lpAdmin = new LiquidityProviderAdminImpl(lp);
		}
		return lpAdmin;
	}
	
	private final ConcurrentMap<LiquidityProvider, LiquidityProviderAdminImpl> lpAdmins = new ConcurrentHashMap<>();
	
	
	protected void onLpRegistrationEvent(LiquidityProvidersRegistry.LpRegistrationEvent event) {
		lpEventsSupport.publish(new LiquidityProviderEvent(event.liquidityProvider().info(), event.eventType() == LiquidityProvidersRegistry.LpRegistrationEvent.EventType.REGISTERED
				? LiquidityProviderEvent.EventType.REGISTERED : LiquidityProviderEvent.EventType.DEREGISTERED), true);
	}
	
	
	public LiquidityProvidersAdminImpl(LiquidityProvidersRegistry registry) {
		this.registry = registry;
		
		registry().subscribe(this.lpRegistrationListener);
	}
	
	private final LiquidityProvidersRegistry registry;
	protected LiquidityProvidersRegistry registry() { return registry; }
	
	private final EventsSubscriptionSupport<LiquidityProviderEvent> lpEventsSupport = new EventsSubscriptionSupport<>();
	
	private final Consumer<LiquidityProvidersRegistry.LpRegistrationEvent> lpRegistrationListener = this::onLpRegistrationEvent;
	
}
