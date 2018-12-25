package network.piranya.platform.node.core.execution.engine;

import java.util.function.Consumer;

import network.piranya.platform.api.models.trading.ExecutionEvent;
import network.piranya.platform.api.models.trading.liquidity.ExchangeTradeEvent;
import network.piranya.platform.api.models.trading.liquidity.QuoteEvent;
import network.piranya.platform.node.api.local_infrastructure.storage.LocalStorage;
import network.piranya.platform.node.api.local_infrastructure.storage.PersistentQueue;
import network.piranya.platform.node.core.execution.engine.activity_log.EsLog;
import network.piranya.platform.node.core.execution.engine.activity_log.BotAbortedLog;
import network.piranya.platform.node.core.execution.engine.activity_log.BotCommandedLog;
import network.piranya.platform.node.core.execution.engine.activity_log.BotCreatedLog;
import network.piranya.platform.node.core.execution.engine.activity_log.BotFinishedLog;
import network.piranya.platform.node.core.execution.engine.activity_log.FillLog;
import network.piranya.platform.node.core.execution.engine.activity_log.OrderCancelReplyLog;
import network.piranya.platform.node.core.execution.engine.activity_log.OrderPlacementReplyLog;
import network.piranya.platform.node.utilities.Encoder;

public class ExecutionTrail {
	
	public void append(EsLog log) {
		queue().append(encoder().encode(log));
	}
	
	public void append(ExecutionEvent event) {
		queue().append(encoder().encode(event));
	}
	
	public void replay(Consumer<EsLog> logAcceptor, Consumer<ExecutionEvent> eventAcceptor) {
		byte[] buffer = new byte[100 * 1024];
		queue().read(buffer, len -> {
			Object obj = encoder().decode(buffer, 0, len);
			if (obj instanceof EsLog) {
				logAcceptor.accept((EsLog)obj);
			} else if (obj instanceof ExecutionEvent) {
				eventAcceptor.accept((ExecutionEvent)obj);
			}
		});
	}
	
	public void dispose() {
		queue().release(this);
	}
	
	
	public ExecutionTrail(LocalStorage localStorage) {
		this.queue = initQueue(localStorage);
		
		this.encoder = new Encoder();
		encoder().registerDataType(BotCreatedLog.class, (short)1001);
		encoder().registerDataType(BotAbortedLog.class, (short)1002);
		encoder().registerDataType(BotFinishedLog.class, (short)1003);
		encoder().registerDataType(BotCommandedLog.class, (short)1004);
		encoder().registerDataType(OrderPlacementReplyLog.class, (short)2001);
		encoder().registerDataType(OrderCancelReplyLog.class, (short)2002);
		encoder().registerDataType(FillLog.class, (short)2003);
		encoder().registerDataType(QuoteEvent.class, (short)2010);
		encoder().registerDataType(ExchangeTradeEvent.class, (short)2011);
	}
	
	protected PersistentQueue initQueue(LocalStorage localStorage) {
		return localStorage.persistentQueue("system#execution_engine", new PersistentQueue.Config(), this);
	}
	
	private final PersistentQueue queue;
	protected PersistentQueue queue() {
		return queue;
	}
	
	private final Encoder encoder;
	protected Encoder encoder() {
		return encoder;
	}
	
}
