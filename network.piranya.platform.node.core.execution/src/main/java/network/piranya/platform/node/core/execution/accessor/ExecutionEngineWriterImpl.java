package network.piranya.platform.node.core.execution.accessor;

import network.piranya.platform.api.models.execution.ExecutionEngineWriter;
import network.piranya.platform.node.api.execution.ExecutionManager;
import network.piranya.platform.node.api.execution.bots.BotsRegistry;

public class ExecutionEngineWriterImpl extends ExecutionEngineAccessor implements ExecutionEngineWriter {
	
	public ExecutionEngineWriterImpl(ExecutionManager executionManager, BotsRegistry agentsRegistry) {
		super(executionManager, agentsRegistry);
	}
	
}
