package network.piranya.platform.api.models.info;

import java.math.BigDecimal;
import java.util.Collection;

import network.piranya.platform.api.extension_models.InputType;
import network.piranya.platform.api.lang.ResultFuture;
import network.piranya.platform.api.models.trading.Asset;
import network.piranya.platform.api.models.trading.Instrument;

public interface MarketInfoProvider {
	
	BigDecimal pip(Instrument instrument);
	
	BigDecimal pipValueIn(Instrument instrument, Asset currency);
	
	ResultFuture<Collection<AssetInfo>> findAssets(String keyword);
	ResultFuture<Collection<Instrument>> findInstruments(String keyword);
	ResultFuture<Collection<Instrument>> findInstruments(String keyword, InputType type);
	ResultFuture<Collection<ExchangeInfo>> listExchangesforAsset(Asset asset);
	ResultFuture<Collection<ExchangeInfo>> listExchangesforInstrument(Instrument instrument);
	ResultFuture<Collection<Instrument>> listInstrumentsforAsset(Asset asset);
	
	AssetInfo getAssetInfo(String symbol);
	// Iterator assets(), tradingPairs
	
}
