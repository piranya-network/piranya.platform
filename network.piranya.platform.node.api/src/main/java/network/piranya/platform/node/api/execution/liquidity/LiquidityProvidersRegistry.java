package network.piranya.platform.node.api.execution.liquidity;

import java.util.List;
import java.util.function.Consumer;

import network.piranya.platform.api.exceptions.SuitableLpNotFoundException;
import network.piranya.platform.api.extension_models.Parameters;
import network.piranya.platform.api.extension_models.execution.liquidity.LiquidityProvider;
import network.piranya.platform.api.models.metadata.LiquidityProviderTypeInfo;
import network.piranya.platform.api.models.trading.Instrument;

public interface LiquidityProvidersRegistry {
	
	<LiquidityProviderType extends LiquidityProvider> void registerLiquidityProviderType(Class<LiquidityProviderType> liquidityProviderType);
	<LiquidityProviderType extends LiquidityProvider> void deregisterLiquidityProviderType(Class<LiquidityProviderType> liquidityProviderType);
	List<Class<LiquidityProvider>> liquidityProviderTypes();
	List<LiquidityProviderTypeInfo> liquidityProviderTypesInfo();
	LiquidityProviderTypeInfo liquidityProviderTypeInfo(String liquidityProviderTypeId);
	
	LiquidityProvider registerLiquidityProvider(String liquidityProviderType, Parameters providerParams);
	void deregisterLiquidityProvider(String liquidityProviderId);
	List<LiquidityProvider> liquidityProviders();
	LiquidityProvider liquidityProvider(String liquidityProviderId);
	
	void subscribe(Consumer<LpRegistrationEvent> listener);
	void unsubscribe(Consumer<LpRegistrationEvent> listener);
	
	LiquidityProvider liquidityProviderForInstrument(Instrument instrument) throws SuitableLpNotFoundException;
	List<LiquidityProvider> liquidityProvidersForInstrument(Instrument instrument);
	
	
	public static class LpRegistrationEvent {
		
		public static enum EventType { REGISTERED, DEREGISTERED }
		
		public LpRegistrationEvent(LiquidityProvider liquidityProvider, EventType eventType) {
			this.liquidityProvider = liquidityProvider;
			this.eventType = eventType;
		}
		
		private final LiquidityProvider liquidityProvider;
		public LiquidityProvider liquidityProvider() {
			return liquidityProvider;
		}
		
		private final EventType eventType;
		public EventType eventType() {
			return eventType;
		}
	}
	
}
