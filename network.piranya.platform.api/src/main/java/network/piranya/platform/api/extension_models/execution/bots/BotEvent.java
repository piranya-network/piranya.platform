package network.piranya.platform.api.extension_models.execution.bots;

import network.piranya.platform.api.lang.Optional;

import network.piranya.platform.api.extension_models.Data;
import network.piranya.platform.api.models.bots.BotRef;
import network.piranya.platform.api.models.trading.ExecutionEvent;

public final class BotEvent implements ExecutionEvent {
	
	private final BotRef botRef;
	public BotRef botRef() { return botRef; }
	
	private final String botTypeId;
	public String botTypeId() { return botTypeId; }
	
	private final String eventTypeId;
	public String eventTypeId() { return eventTypeId; }
	
	private final Optional<String> errorDescription;
	public Optional<String> errorDescription() { return errorDescription; }
	
	private final Data data;
	public Data data() { return data; }
	
	public boolean isFinishedEvent() {
		return eventTypeId().equals(FINISHED_EVENT_TYPE_ID) || eventTypeId().equals(ABORTED_EVENT_TYPE_ID);
	}
	
	
	public BotEvent(BotRef botRef, String botTypeId, String eventTypeId, Optional<String> errorDescription, Data data) {
		this.botRef = botRef;
		this.botTypeId = botTypeId;
		this.eventTypeId = eventTypeId;
		this.errorDescription = errorDescription;
		this.data = data;
	}
	
	public BotEvent(BotRef actorRef, String botTypeId, String eventTypeId, Data data) {
		this(actorRef, botTypeId, eventTypeId, Optional.empty(), data);
	}
	
	public static final String STARTED_EVENT_TYPE_ID = "BotEvent:STARTED";
	public static final String FINISHED_EVENT_TYPE_ID = "BotEvent:FINISHED";
	public static final String ABORTED_EVENT_TYPE_ID = "BotEvent:ABORTED";
	public static final String PARAMS_UPDATED_EVENT_TYPE_ID = "BotEvent:PARAMS_UPDATED";
	
}
