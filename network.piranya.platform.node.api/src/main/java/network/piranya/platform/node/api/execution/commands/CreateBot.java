package network.piranya.platform.node.api.execution.commands;

import java.util.function.Consumer;

import network.piranya.platform.api.extension_models.Parameters;
import network.piranya.platform.api.extension_models.execution.bots.BotEvent;
import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.api.lang.Result;
import network.piranya.platform.api.models.bots.BotSpec;
import network.piranya.platform.api.models.bots.BotView;

public class CreateBot implements ExecutionCommand {
	
	private final Optional<String> botTypeId;
	public Optional<String> botTypeId() {
		return botTypeId;
	}
	
	private final Optional<String> featureId;
	public Optional<String> featureId() {
		return featureId;
	}
	
	private final Parameters params;
	public Parameters params() {
		return params;
	}
	
	private final Optional<Consumer<BotEvent>> eventsSubscriber;
	public Optional<Consumer<BotEvent>> eventsSubscriber() {
		return eventsSubscriber;
	}
	
	private final Consumer<Result<BotView>> resultHandler;
	public Consumer<Result<BotView>> resultHandler() {
		return resultHandler;
	}
	
	private CreateBot(Optional<String> botTypeId, Optional<String> featureId, Parameters params, Optional<Consumer<BotEvent>> eventsSubscriber, Consumer<Result<BotView>> resultHandler) {
		this.botTypeId = botTypeId;
		this.featureId = featureId;
		this.params = params;
		this.eventsSubscriber = eventsSubscriber;
		this.resultHandler = resultHandler;
	}
	
	public static CreateBot createByType(String botTypeId, Parameters params, Optional<Consumer<BotEvent>> eventsSubscriber, Consumer<Result<BotView>> resultHandler) {
		return new CreateBot(Optional.of(botTypeId), Optional.empty(), params, eventsSubscriber, resultHandler);
	}
	
	public static CreateBot createByFeature(String featureId, Parameters params, Optional<Consumer<BotEvent>> eventsSubscriber, Consumer<Result<BotView>> resultHandler) {
		return new CreateBot(Optional.empty(), Optional.of(featureId), params, eventsSubscriber, resultHandler);
	}
	
	public static CreateBot create(BotSpec spec, Parameters params, Optional<Consumer<BotEvent>> eventsSubscriber, Consumer<Result<BotView>> resultHandler) {
		return new CreateBot(spec.botTypeId(), spec.featureId(), params, eventsSubscriber, resultHandler);
	}
	
}
