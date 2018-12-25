package network.piranya.platform.node.core.execution.commands;

import java.util.List;

import network.piranya.platform.api.extension_models.ExtensionException;
import network.piranya.platform.api.extension_models.Parameters;
import network.piranya.platform.api.extension_models.execution.bots.Command;
import network.piranya.platform.api.extension_models.execution.bots.CommandBot;
import network.piranya.platform.api.lang.Result;
import network.piranya.platform.api.lang.ResultHandler;
import network.piranya.platform.api.models.bots.BotSpec;
import network.piranya.platform.node.core.execution.bots.BasicBot;

public class BasicCommandBot extends CommandBot {
	
	@Command(id = "start_bot")
	public void startBot() {
		context().bots().execution().startBot(BotSpec.byType(BasicBot.class.getName()), utils.params().build(), r -> {});
	}
	
	@Command(id = "query_lps")
	public List<LpInfo> queryLps() {
		return utils.col.map(context().liquidityProvidersAdmin().liquidityProviders(), lp -> new LpInfo(lp.info().getId()));
	}
	
	@Command(id = "ping")
	public String ping(Parameters params) {
		return "pong " + params.string("message");
	}
	
	@Command(id = "ping_async")
	public void ping(ResultHandler<String> resultHandler) {
		resultHandler.accept(new Result<>("pong"));
	}
	
	@Command(id = "exceptional_command")
	public void queryCommandAsync() {
		throw new ExtensionException("error");
	}
	
}
