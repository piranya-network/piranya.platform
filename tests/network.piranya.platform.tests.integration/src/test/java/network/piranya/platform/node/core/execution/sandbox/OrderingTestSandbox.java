package network.piranya.platform.node.core.execution.sandbox;

import java.math.BigDecimal;

import network.piranya.platform.api.extension_models.ParametersBuilder;
import network.piranya.platform.api.extension_models.execution.bots.BotEvent;
import network.piranya.platform.api.extension_models.execution.sandbox.SandboxConfiguration;
import network.piranya.platform.api.lang.OpenEndedPeriod;
import network.piranya.platform.node.core.execution.trading.MarketOrderBot;

public class OrderingTestSandbox extends SandboxConfiguration {
	
	@Override
	public void configure(ConfigContext context) {
		context.inheritAllLiquidityProviders();
		//context.registerLiquidityProvider("MockLp", utils().params().string("liquidityProviderId", params().string("liquidityProviderId")).integer("latency", 100).build());
		
		context.inheritAllBotTypes();
		context.registerBotType(StatsGatheringBot.class);
		
		context.runBot(StatsGatheringBot.class.getName(), utils().params().build());
		context.runBot(MarketOrderBot.class.getName(), utils().params().string("lp", "LP0").string("symbol", "S/B").bool("is_buy", true)
				.decimal("price", new BigDecimal("1200")).decimal("size", new BigDecimal("1.0")).build());
		
		context.registerAnalyticalView("statsView", OrdersCounterAnalyticalView.class, OpenEndedPeriod.fixedStartTime(0), new ParametersBuilder().build());
		
		context.finishCondition(BotEvent.class, e -> e.botTypeId().equals(MarketOrderBot.class.getName()) && e.isFinishedEvent());
	}
	
}
