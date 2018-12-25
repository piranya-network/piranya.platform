package network.piranya.platform.node.core.execution.engine;

import static network.piranya.platform.node.utilities.CollectionUtils.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import network.piranya.platform.api.exceptions.PiranyaException;
import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.api.models.log.ActivityLog;
import network.piranya.platform.api.models.trading.Instrument;
import network.piranya.platform.api.models.trading.filling.Fill;
import network.piranya.platform.api.models.trading.filling.OpenTrade;
import network.piranya.platform.api.models.trading.ordering.PendingOrder;
import network.piranya.platform.api.models.trading.ordering.OrderRef;
import network.piranya.platform.api.models.trading.ordering.OrderingProgress;
import network.piranya.platform.node.core.execution.engine.activity_log.FillLog;
import network.piranya.platform.node.utilities.CollectionUtils;

public class TradingRepository {
	
	public Collection<PendingOrder> orders() {
		return orders.values();
	}

	public void addOrder(PendingOrder order) {
		orders.put(order.orderRef(), order);
	}
	
	public PendingOrder removeOrder(OrderRef orderRef) {
		return orders.remove(orderRef);
	}
	
	public PendingOrder get(OrderRef orderRef) {
		PendingOrder order = orders.get(orderRef);
		if (order == null) {
			throw new PiranyaException(String.format("Order '%s' was not found", orderRef));
		}
		return order;
	}
	
	public Optional<PendingOrder> findOrderByExternalId(String externalOrderId, String liquidityProviderId) {
		return find(orders.values(), o -> externalOrderId.equals(o.externalOrderRef().externalOrderId())
				&& liquidityProviderId.equals(o.externalOrderRef().lpRef().liquidityProviderId()));
	}
	
	
	public Fill acceptFill(PendingOrder order, FillLog log) {
		AccumulatedFillsInfo accumulatedFillsInfo = getValue(ordersAccumulatedFills, order.orderRef(), () -> new AccumulatedFillsInfo(), false);
		accumulatedFillsInfo.addFill(log.size());
		
		String tradeId = order.description().tradeId().orElse(log.liquidityProviderId() + ":" + log.externalTradeId());
		Fill fill = new Fill(fillId(accumulatedFillsInfo, order), tradeId, log.externalFillId(),
				log.externalTradeId(), log.liquidityProviderId(), log.symbol(), log.price(), log.size(), log.fees(), log.time(), order);
		OpenTrade trade = addFill(getValue(tradesMap, tradeId, () -> new OpenTrade(tradeId, new Instrument(fill.symbol()), list(), fill.order().description().tags()), false), fill);
		
		/// log trade entered activity if first fill
		if (trade.fills().size() == 1) {
			//logActivity().accept(new TradeEn);
		}
		
		if (sumSize(trade).doubleValue() != 0.0) {
			tradesMap.put(tradeId, trade);
		} else {
			tradesMap.remove(tradeId);
		}
		
		if (accumulatedFillsInfo.accumulatedSize().abs().compareTo(order.spec().size()) >= 0) {
			orders.remove(order.orderRef());
			ordersAccumulatedFills.remove(order.orderRef());
		} else {
			orders.put(order.orderRef(), addPartialFill(order, fill));
		}
		
		return fill;
	}
	
	protected String fillId(AccumulatedFillsInfo accumulatedFillsInfo, PendingOrder order) {
		return String.format("%s.F%s", order.orderRef(), accumulatedFillsInfo.currentFillIndex());
	}
	
	public List<OpenTrade> trades() {
		return new ArrayList<>(tradesMap.values());
	}
	
	public List<Fill> fills(String liquidityProviderId) {
		List<Fill> fills = new ArrayList<>();
		for (OpenTrade t : tradesMap.values()) {
			for (Fill f : t.fills()) {
				if (f.liquidityProviderId().equals(liquidityProviderId)) {
					fills.add(f);
				}
			}
		}
		return fills;
	}
	
	public List<PendingOrder> orders(String liquidityProviderId) {
		return CollectionUtils.filter(orders(), o -> o.externalOrderRef().lpRef().liquidityProviderId().equals(liquidityProviderId));
	}
	
	
	// TODO re-consider
	public void merge(List<String> externalTradeIds, String resultantExternalTradeId/*consider passing price/volume*/) {
		// unify internal trade id, and update external trade id
		throw new RuntimeException("Not Implemented");
	}
	
	
	protected OpenTrade addFill(OpenTrade trade, Fill fill) {
		HashSet<String> tags = new HashSet<>(trade.tags());
		tags.addAll(fill.order().description().tags());
		return new OpenTrade(trade.tradeId(), new Instrument(fill.symbol()), add(trade.fills(), fill), tags);
	}
	
	protected PendingOrder addPartialFill(PendingOrder order, Fill fill) {
		return new PendingOrder(order.orderRef(), order.creatorBotRef(), order.spec(), order.externalOrderRef(), order.description(), order.createAt(),
				new OrderingProgress(OrderingProgress.PARTIALLY_FILLED_STATUS, "Partially Filled", order.progress().filledSize().add(fill.size()), order.progress().details()));
	}
	
	protected BigDecimal sumSize(OpenTrade trade) {
		return summarize(trade.fills(), f -> f.size(), new BigDecimal(0), (sum, f) -> sum.add(f));
	}
	
	
	// incorporate LP orders/trades. call on start (after replay and catchup) and on re-connection.
		// what about order/trade added? if added order/trade was not placed/cancel from here, call
	public void incorporateLp(Object lpState) {
		// TODO
	}
	
	
	public TradingRepository(Consumer<ActivityLog> logActivity) {
		this.logActivity = logActivity;
	}
	
	private final Map<OrderRef, PendingOrder> orders = new HashMap<>();
	private final Map<OrderRef, AccumulatedFillsInfo> ordersAccumulatedFills = new HashMap<>();
	private final Map<String, OpenTrade> tradesMap = new HashMap<>();
	
	private final Consumer<ActivityLog> logActivity;
	protected Consumer<ActivityLog> logActivity() { return logActivity; }
	
	
	protected class AccumulatedFillsInfo {
		
		public void addFill(BigDecimal size) {
			++currentFillIndex;
			accumulatedSize = size.add(accumulatedSize);
		}
		
		public int currentFillIndex() { return currentFillIndex; }
		public BigDecimal accumulatedSize() { return accumulatedSize; }
		
		private int currentFillIndex = -1;
		private BigDecimal accumulatedSize = new BigDecimal(0);
	}
	
}
