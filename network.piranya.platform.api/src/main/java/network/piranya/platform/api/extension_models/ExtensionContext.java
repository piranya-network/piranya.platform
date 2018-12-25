package network.piranya.platform.api.extension_models;

import network.piranya.platform.api.extension_models.execution.sandbox.SandboxConfiguration;
import network.piranya.platform.api.models.analytics.Analytics;
import network.piranya.platform.api.models.bots.BotsAdmin;
import network.piranya.platform.api.models.execution.ExecutionEngineReader;
import network.piranya.platform.api.models.execution.sandbox.Sandbox;
import network.piranya.platform.api.models.info.MarketInfoProvider;
import network.piranya.platform.api.models.infrastructure.Executor;
import network.piranya.platform.api.models.infrastructure.SerializationServices;
import network.piranya.platform.api.models.infrastructure.net.NetServices;
import network.piranya.platform.api.models.infrastructure.storage.StorageServices;
import network.piranya.platform.api.models.log.ActivityLogStore;
import network.piranya.platform.api.models.search.SearchService;

public interface ExtensionContext {
	
	BotsAdmin bots();
	ExecutionEngineReader executionEngine();
	
	Analytics analytics();
	
	StorageServices storage();
	NetServices net();
	Executor executor();
	SerializationServices serialization();
	
	ActivityLogStore activityLog();
	
	MarketInfoProvider marketInfo();
	
	// subscription to events or bot events
	// publish events to execution engine?
	
	<SandboxConfigurationClass extends SandboxConfiguration> Sandbox createSandbox(SandboxConfigurationClass sandboxConfig);
	
	SearchService search();
	
}
