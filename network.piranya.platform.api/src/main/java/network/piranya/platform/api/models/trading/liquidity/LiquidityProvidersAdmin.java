package network.piranya.platform.api.models.trading.liquidity;

import java.util.List;
import java.util.function.Consumer;

import network.piranya.platform.api.exceptions.SuitableLpNotFoundException;
import network.piranya.platform.api.extension_models.Parameters;
import network.piranya.platform.api.models.metadata.LiquidityProviderInfo;
import network.piranya.platform.api.models.metadata.LiquidityProviderTypeInfo;
import network.piranya.platform.api.models.trading.Instrument;

public interface LiquidityProvidersAdmin {
	
	List<LiquidityProviderAdmin> liquidityProviders();
	LiquidityProviderAdmin liquidityProvider(String liquidityProviderId);
	
	LiquidityProviderAdmin register(String liquidityProviderTypeId, Parameters params);
	void deregister(String liquidityProviderId);
	
	List<LiquidityProviderTypeInfo> liquidityProvidersTypes();
	LiquidityProviderTypeInfo liquidityProviderType(String liquidityProviderTypeId);
	
	void subscribe(Consumer<LiquidityProviderEvent> subscriber);
	void unsubscribe(Consumer<LiquidityProviderEvent> subscriber);
	
	LiquidityProviderAdmin liquidityProviderForInstrument(Instrument instrument) throws SuitableLpNotFoundException;
	List<LiquidityProviderAdmin> liquidityProvidersForInstrument(Instrument instrument);
	
	
	public static class LiquidityProviderEvent {
		
		public static enum EventType { REGISTERED, DEREGISTERED }
		
		public LiquidityProviderEvent(LiquidityProviderInfo lpInfo, EventType eventType) {
			this.lpInfo = lpInfo;
			this.eventType = eventType;
		}
		
		private final LiquidityProviderInfo lpInfo;
		public LiquidityProviderInfo lpInfo() { return lpInfo; }
		
		private final EventType eventType;
		public EventType eventType() { return eventType; }
	}
	
}
