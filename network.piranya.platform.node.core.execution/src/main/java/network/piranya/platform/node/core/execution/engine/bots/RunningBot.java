package network.piranya.platform.node.core.execution.engine.bots;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import network.piranya.platform.api.extension_models.execution.bots.ExecutionBot;
import network.piranya.platform.api.extension_models.execution.bots.BotEvent;

public class RunningBot {
	
	/// move later to state object for actor that can be snapshot
	public String nextOrderId() {
		return String.format("%s:%s", bot.ref().botId(), ++currentOrderId);
	}
	private long currentOrderId = 0;
	
	public RunningBot(ExecutionBot bot) {
		this.bot = bot;
	}
	
	private final ExecutionBot bot;
	public ExecutionBot bot() {
		return bot;
	}
	
	private final Set<Consumer<BotEvent>> eventSubscribers = new HashSet<>();
	public Set<Consumer<BotEvent>> eventSubscribers() {
		return eventSubscribers;
	}
	
	private final Consumer<Consumer<BotEvent>> subscribeOperation = subscriber -> eventSubscribers().add(subscriber);
	public Consumer<Consumer<BotEvent>> subscribeOperation() {
		return subscribeOperation;
	}
	
	private final Consumer<Consumer<BotEvent>> unsubscribeOperation = subscriber -> eventSubscribers().remove(subscriber);
	public Consumer<Consumer<BotEvent>> unsubscribeOperation() {
		return unsubscribeOperation;
	}
	
	public void dispose() {
	}
	
}