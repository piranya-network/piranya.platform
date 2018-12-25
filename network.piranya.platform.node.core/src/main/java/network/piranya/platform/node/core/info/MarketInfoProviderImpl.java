package network.piranya.platform.node.core.info;

import static network.piranya.platform.node.utilities.CollectionUtils.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import network.piranya.platform.api.Constants;
import network.piranya.platform.api.exceptions.PiranyaException;
import network.piranya.platform.api.exceptions.UnexpectedException;
import network.piranya.platform.api.extension_models.InputType;
import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.api.lang.ResultFuture;
import network.piranya.platform.api.models.info.AssetInfo;
import network.piranya.platform.api.models.info.ExchangeInfo;
import network.piranya.platform.api.models.info.MarketInfoProvider;
import network.piranya.platform.api.models.trading.Asset;
import network.piranya.platform.api.models.trading.Instrument;
import network.piranya.platform.node.api.local_infrastructure.LocalServices;
import network.piranya.platform.node.api.local_infrastructure.LocalServicesProvider;
import network.piranya.platform.node.api.local_infrastructure.concurrency.Executor;
import network.piranya.platform.node.core.execution.engine.infrastructure.net.NetServicesImpl;
import network.piranya.platform.node.core.execution.infrastructure.serialization.json.JsonStreamReaderImpl;
import network.piranya.platform.node.core.info.MarketcapTicker.Quote;
import network.piranya.platform.node.utilities.CollectionUtils;
import network.piranya.platform.node.utilities.Json;
import network.piranya.platform.node.utilities.StringUtils;
import network.piranya.platform.node.utilities.impl.ResultFutureImpl;

public class MarketInfoProviderImpl implements MarketInfoProvider {
	
	@Override
	public BigDecimal pip(Instrument instrument) {
		return new BigDecimal("0.000000001");
	}
	
	@Override
	public BigDecimal pipValueIn(Instrument instrument, Asset targetCurrency) {
		BigDecimal targetRate = getCurrencyExchangeRate(instrument.quote().symbol(), targetCurrency.symbol());
		
		//return spec.getPip().multiply(spec.getContractSize()).multiply(spec.getPartOfCurrency()).multiply(targetRate);
		return pip(instrument).multiply(targetRate);
	}
	
	@Override
	public ResultFuture<Collection<AssetInfo>> findAssets(String keyword) {
		SortedSet<AssetInfo> result = new TreeSet<>();
		AssetInfo exactMatch = null;
		keyword = keyword.trim().toUpperCase();
		if (keyword.isEmpty()) {
			return new ResultFutureImpl<>(result);
		}
		
		for (AssetDetails a : assetsMap().values()) {
			if (a.symbol().equals(keyword) || a.displayNameUpperCase().equals(keyword)) {
				exactMatch = mapAssetDetails(a);
				result.add(exactMatch);
			} else if (a.symbol().contains(keyword) || a.displayNameUpperCase().contains(keyword)) {
				result.add(mapAssetDetails(a));
			} else {
				for (String alt : a.altSymbols()) {
					if (alt.contains(keyword)) {
						result.add(mapAssetDetails(a));
						break;
					}
				}
			}
		}
		
		List<AssetInfo> listResult = new ArrayList<>(result);
		if (exactMatch != null) {
			listResult.remove(exactMatch);
			listResult.add(0, exactMatch);
		}
		return new ResultFutureImpl<>(listResult);
	}
	
	@Override
	public ResultFuture<Collection<Instrument>> findInstruments(String keyword) {
		return findInstruments(keyword, InputType.INSTRUMENT);
	}
	
	@Override
	public ResultFuture<Collection<Instrument>> findInstruments(String keyword, InputType type) {
		List<Instrument> result = new ArrayList<>();
		keyword = keyword.toUpperCase();
		if (keyword.indexOf('/') < 0) {
			/*if (keyword.trim().indexOf(" FOR ") > 0) {
				keyword = StringUtils.replace(keyword, " FOR ", "/");
			} else */if (keyword.endsWith(" ")) {
				keyword = keyword.trim() + "/";
			}
		}
		keyword = keyword.trim();
		keyword = StringUtils.replace(keyword, " ", Constants.Instrument.BASE_QUOTE_SEPARATOR);
		
		if (type == InputType.AVAILABLE_INSTRUMENT) {
			try {
				for (Instrument instrument : registeredInstruments()) {
					if (/*instrument.symbol().startsWith(keyword)*/instrument.symbol().indexOf(keyword) >= 0) {
						result.add(instrument);
					}
				}
			} catch (Exception ex) {
			}
		} else {
			try {
				Instrument instrument = new Instrument(keyword);
				result.addAll(findMatchingInstruments(instrument.base().symbol(), instrument.quote().symbol()));
				result.addAll(findMatchingInstruments(instrument.quote().symbol(), instrument.base().symbol()));
			} catch (Exception ex) {
				try {
					for (int i = 0; i < keyword.length() - 1; i++) {
						if (assetsMap().containsKey(keyword.substring(0, i))) {
							Instrument instrument = new Instrument(keyword.substring(0, i) + Constants.Instrument.BASE_QUOTE_SEPARATOR + keyword.substring(i, keyword.length()));
							result.addAll(findMatchingInstruments(instrument.base().symbol(), instrument.quote().symbol()));
							result.addAll(findMatchingInstruments(instrument.quote().symbol(), instrument.base().symbol()));
						}
					}
				} catch (Exception ex2) {
				}
			}
			
			SortedSet<Instrument> instrumentsSet = new TreeSet<>();
			if (type == InputType.INSTRUMENT) {
				for (Instrument instrument : result) {
					instrumentsSet.add(new Instrument(new Asset(instrument.base().symbol()), new Asset(instrument.quote().symbol())));
				}
			} else {
				instrumentsSet.addAll(result);
			}
			return new ResultFutureImpl<>(instrumentsSet);
		}
		return new ResultFutureImpl<>(result);
	}
	
	@Override
	public ResultFuture<Collection<ExchangeInfo>> listExchangesforAsset(Asset asset) {
		AssetDetails assetDetails = assetsMap().get(asset.symbol());
		if (assetDetails != null) {
			return new ResultFutureImpl<>(map(sort(set(map(assetDetails.tradedInstruments(), i -> i.sourceId()))), exchangeId -> new ExchangeInfo(exchangeId)));
		} else {
			return new ResultFutureImpl<>(set());
		}
	}
	
	@Override
	public ResultFuture<Collection<ExchangeInfo>> listExchangesforInstrument(Instrument instrument) {
		AssetDetails assetDetails = assetsMap().get(instrument.base().symbol());
		if (assetDetails != null) {
			return new ResultFutureImpl<>(map(sort(set(map(filter(assetDetails.tradedInstruments(), i -> i.base().equals(instrument.base()) && i.quote().equals(instrument.quote())),
					i -> i.sourceId()))), exchangeId -> new ExchangeInfo(exchangeId)));
		} else {
			return new ResultFutureImpl<>(set());
		}
	}
	
	@Override
	public ResultFuture<Collection<Instrument>> listInstrumentsforAsset(Asset asset) {
		SortedSet<Instrument> instrumentsSet = new TreeSet<>();
		for (Instrument instrument : getAssetInfo(asset.symbol()).tradedInstruments()) {
			instrumentsSet.add(new Instrument(new Asset(instrument.base().symbol()), new Asset(instrument.quote().symbol())));
		}
		return new ResultFutureImpl<>(instrumentsSet);
	}
	
	@SuppressWarnings("unchecked")
	protected List<Instrument> findMatchingInstruments(String base, String quote) {
		AssetDetails assetDetails = assetsMap().get(base);
		if (assetDetails != null) {
			List<Instrument> result = new ArrayList<>();
			for (Instrument instrument : assetDetails.tradedInstruments()) {
				if (instrument.quote().symbol().startsWith(quote)) {
					result.add(instrument);
				}
			}
			return result;
		} else {
			return Collections.EMPTY_LIST;
		}
	}
	
	protected List<Instrument> findMatchingRegisteredInstruments(String base, String quote) {
		List<Instrument> result = new ArrayList<>();
		for (Instrument instrument : registeredInstruments()) {
			if ((instrument.base().symbol().startsWith(base) && instrument.quote().symbol().startsWith(quote))
					|| (instrument.quote().symbol().startsWith(base) && instrument.base().symbol().startsWith(quote))) {
				
			}
		}
		return result;
	}
	
	@Override
	public AssetInfo getAssetInfo(String symbol) {
		Optional<AssetDetails> a = findAsset(symbol);
		return a.map(this::mapAssetDetails).orElseThrow();
	}
	
	public void registerInstrument(Instrument instrument, String lpId) {
		if (!lpId.equals(instrument.sourceId())) {
			instrument = instrument.appendSource(lpId);
		}
		
		registeredInstruments().add(instrument);
	}
	
	public void deregisterLpInstruments(String lpId) {
		
	}
	
	private final SortedSet<Instrument> registeredInstruments = new TreeSet<>();
	protected SortedSet<Instrument> registeredInstruments() { return registeredInstruments; }
	
	protected AssetInfo mapAssetDetails(AssetDetails a) {
		return new AssetInfo(a.symbol(), a.altSymbols(), a.displayName(), a.tradedInstruments());
	}
	
	public void dispose() {
		netServices.dispose();
		localServices.dispose();
	}
	
	
	protected BigDecimal getCurrencyExchangeRate(String fromAsset, String toAsset) {
		if (fromAsset.equals(toAsset)) {
			return new BigDecimal(1);
		}
		
		AssetDetails fromAssetDetails = findAsset(fromAsset).orElseThrow(() -> new PiranyaException(String.format("Asset '%s' not found", fromAsset)));
		if (toAsset.equals("BTC")) {
			return fromAssetDetails.priceInBtc().orElseThrow(() -> new PiranyaException(String.format("Price in BTC for '%s' is not available", fromAssetDetails.symbol())));
		} else if (toAsset.equals("USD")) {
			return fromAssetDetails.priceInUsd().orElseThrow(() -> new PiranyaException(String.format("Price in USD for '%s' is not available", fromAssetDetails.symbol())));
		} else {
			throw new PiranyaException("Exchange rate is only supported to BTC and USD currently");
		}
		/*
		 *	Quote prices = getLastQuote(generateCurrencyPairId(fromCurrency, toCurrency));
		
			if (prices != null) {
				return prices.getBid().add(prices.getAsk()).divide(new BigDecimal(2), RoundingMode.HALF_UP);
			} else {
				prices = getLastQuote(generateCurrencyPairId(toCurrency, fromCurrency));
				
				if (prices == null && !fromCurrency.equals(USD) && !toCurrency.equals(USD)) {
					BigDecimal fromUsdRate = getCurrencyExchangeRate(fromCurrency, USD);
					BigDecimal toUsdRate = getCurrencyExchangeRate(USD, toCurrency);
					
					return fromUsdRate.multiply(toUsdRate);
				} else {
					return new BigDecimal(1).setScale(6).divide(prices.getBid().add(prices.getAsk()).divide(new BigDecimal(2), RoundingMode.HALF_UP), RoundingMode.HALF_UP);
				}
			}
		 */
	}
	
	
	protected void init() {
		try {
			CoinApiAsset[] assetsData = Json.fromJson(getClass().getResourceAsStream("/META-INF/market-info/coinapi-assets.json"), CoinApiAsset[].class);
			
			Map<String, AssetDetails> assetsMap = new HashMap<>();
			for (CoinApiAsset a : assetsData) {
				HashSet<String> altSymbols = new HashSet<>();
				altSymbols.add(a.getName());
				
				assetsMap.put(a.getAsset_id(), new AssetDetails(a.getAsset_id(), altSymbols, a.getName(), new ArrayList<>()));
			}
			setAssetsMap(assetsMap);
			
			MarketcapTicker marketcapTicker = Json.fromJson(getClass().getResourceAsStream("/META-INF/market-info/marketcap-ticker-to-btc.json"), MarketcapTicker.class);
			for (MarketcapTicker.Entry entry : marketcapTicker.getData().values()) {
				Optional<AssetDetails> details = findAsset(entry.getId());
				if (!details.isPresent()) {
					details = findAsset(entry.getName());
				}
				
				if (details.isPresent()) {
					Quote btcQuote = entry.getQuotes().get("BTC");
					if (btcQuote != null) {
						details.get().setPriceInBtc(btcQuote.getPrice());
					}
					Quote usdQuote = entry.getQuotes().get("USD");
					if (usdQuote != null) {
						details.get().setPriceInUsd(usdQuote.getPrice());
					}
				}
			}
			
			new JsonStreamReaderImpl(getClass().getResourceAsStream("/META-INF/market-info/coinapi-symbols.json")).readArray(CoinApiSymbol.class, symbol -> {
				if (symbol.getSymbol_type().equals("SPOT")) {
					Instrument instrument = new Instrument(new Asset(symbol.getAsset_id_base()), new Asset(symbol.getAsset_id_quote()), symbol.getExchange_id());
					findAsset(instrument.base().symbol()).ifPresent(a -> a.tradedInstruments().add(instrument));
					findAsset(instrument.quote().symbol()).ifPresent(a -> a.tradedInstruments().add(instrument));
				}
			});
		} catch (Throwable ex) {
			throw new UnexpectedException(ex);
		}
	}
	
	protected Optional<AssetDetails> findAsset(String id) {
		AssetDetails details = assetsMap.get(id);
		if (details == null) {
			if (details == null) {
				details = CollectionUtils.find(assetsMap.values(), d -> d.altSymbols().contains(id)).orElse(null);
			}
		}
		return details != null ? Optional.of(details) : Optional.empty();
	}
	
	public MarketInfoProviderImpl(LocalServicesProvider localServicesProvider) {
		this.localServices = localServicesProvider.services(this);
		this.netServices = new NetServicesImpl(localServices.separateExecutor(new Executor.Config(1)));
		
		init();
	}
	
	private final LocalServices localServices;
	private final NetServicesImpl netServices;
	
	private Map<String, AssetDetails> assetsMap;
	protected Map<String, AssetDetails> assetsMap() {
		return assetsMap;
	}
	protected void setAssetsMap(Map<String, AssetDetails> assetsMap) {
		this.assetsMap = assetsMap;
	}
	
}
