package network.piranya.platform.node.core.execution.bots;

import network.piranya.platform.api.extension_models.Data;
import network.piranya.platform.api.extension_models.Parameters;
import network.piranya.platform.api.extension_models.ParametersBuilder;
import network.piranya.platform.api.extension_models.execution.bots.ExecutionBot;
import network.piranya.platform.api.extension_models.execution.bots.BotMetadata;
import network.piranya.platform.api.extension_models.execution.bots.Command;

@BotMetadata(displayName = "Basic Bot", features = "TEST_BOT")
public class BasicBot extends ExecutionBot {
	
	@Override
	public void onStart(ExecutionContext context) {
		if (params().bool("finish_on_start")) {
			context.finish();
		}
	}
	
	@Command(id = "CHANGE_KEY1")
	public void changeKey1(Parameters params) {
		updateState(new ParametersBuilder().string("key1", params.string("key1")).build());
		updateTypedState(new BasicBotState(params.string("key1")));
	}
	
	@Command(id = "PING")
	public Data ping(Parameters params) {
		return new ParametersBuilder().string("result", "pong").build();
	}
	
}
