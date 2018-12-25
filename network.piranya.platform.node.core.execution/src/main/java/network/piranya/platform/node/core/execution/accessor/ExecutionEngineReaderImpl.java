package network.piranya.platform.node.core.execution.accessor;

import network.piranya.platform.api.models.execution.ExecutionEngineReader;
import network.piranya.platform.node.api.execution.ExecutionManager;
import network.piranya.platform.node.api.execution.bots.BotsRegistry;

public class ExecutionEngineReaderImpl extends ExecutionEngineAccessor implements ExecutionEngineReader {
	
	public ExecutionEngineReaderImpl(ExecutionManager executionManager, BotsRegistry agentsRegistry) {
		super(executionManager, agentsRegistry);
	}
	
}
