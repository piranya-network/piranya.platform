package network.piranya.platform.node.core.execution.engine.bots;

import java.util.function.Function;
import java.util.function.Supplier;

import network.piranya.platform.api.extension_models.execution.bots.ExecutionBot;
import network.piranya.platform.api.extension_models.execution.bots.ExecutionBot.ExecutionContext;
import network.piranya.platform.api.models.trading.liquidity.LiquidityProvider;
import network.piranya.platform.api.models.trading.liquidity.LiquidityProvidersListing;
import network.piranya.platform.node.core.execution.engine.LpExectuionBranch;

public class LiquidityProvidersListingImpl implements LiquidityProvidersListing<ExecutionBot.ExecutionContext> {
	
	@Override
	public LiquidityProvider<ExecutionBot.ExecutionContext> get(String liquidityProviderId) {
		context().checkIfDisposed();
		
		return new LiquidityProviderImpl(lpExectuionBranchesProvider().apply(liquidityProviderId), bot(), context(), executionContextSupplier());
	}
	
	
	public LiquidityProvidersListingImpl(Function<String, LpExectuionBranch> lpExectuionBranchesProvider, RunningBot actor,
			AbstractBotContext context, Supplier<ExecutionBot.ExecutionContext> executionContextSupplier) {
		this.lpExectuionBranchesProvider = lpExectuionBranchesProvider;
		this.bot = actor;
		this.context = context;
		this.executionContextSupplier = executionContextSupplier;
	}
	
	private final Function<String, LpExectuionBranch> lpExectuionBranchesProvider;
	protected Function<String, LpExectuionBranch> lpExectuionBranchesProvider() {
		return lpExectuionBranchesProvider;
	}
	
	private final RunningBot bot;
	protected RunningBot bot() {
		return bot;
	}
	
	private final AbstractBotContext context;
	protected AbstractBotContext context() {
		return context;
	}
	
	private final Supplier<ExecutionContext> executionContextSupplier;
	protected Supplier<ExecutionContext> executionContextSupplier() {
		return executionContextSupplier;
	}
	
}
