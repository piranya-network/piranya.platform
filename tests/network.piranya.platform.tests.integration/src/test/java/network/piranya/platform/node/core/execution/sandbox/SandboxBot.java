package network.piranya.platform.node.core.execution.sandbox;

import network.piranya.platform.api.extension_models.execution.bots.ExecutionBot;
import network.piranya.platform.api.extension_models.execution.bots.Command;
import network.piranya.platform.api.models.analytics.AnalyticalViewData;
import network.piranya.platform.api.models.bots.BotView;
import network.piranya.platform.api.models.execution.sandbox.Sandbox;

public class SandboxBot extends ExecutionBot {
	
	@Override
	public void onStart(ExecutionContext context) {
		sandbox = context.createSandbox(new OrderingTestSandbox());
		sandbox.onFinish(this::onSandboxFinished);
		
		updateState(utils().params().integer("fillsCount", 0).build());
		//runSandbox();
	}
	
	@Command(id = "RUN_SANDBOX")
	public void runSandbox() {
		sandbox.runAsync();
	}
	
	@Command(id = "ABORT_SANDBOX")
	public void abortSandbox() {
		sandbox.abort();
	}
	
	
	protected void onSandboxFinished() {
		BotView statsBotView = sandbox.bots().byType(StatsGatheringBot.class.getName()).get(0);
		AnalyticalViewData<OrderingStats> statsView = sandbox.getAnalyticalView("statsView");
		updateState(utils().params().integer("fillsCount", statsBotView.state().integer("fillsCount")).integer("acceptedOrdersCount", statsView.data().getOrdersCount()).build());
	}
	
	
	private Sandbox sandbox;
	
}
