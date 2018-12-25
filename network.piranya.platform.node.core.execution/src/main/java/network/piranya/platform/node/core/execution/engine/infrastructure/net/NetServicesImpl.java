package network.piranya.platform.node.core.execution.engine.infrastructure.net;

import network.piranya.platform.api.models.infrastructure.net.NetServices;
import network.piranya.platform.api.models.infrastructure.net.RestService;
import network.piranya.platform.node.api.local_infrastructure.concurrency.Executor;

public class NetServicesImpl implements NetServices {
	
	@Override
	public RestService rest() {
		return restService;
	}
	
	public void dispose() {
	}
	
	
	public NetServicesImpl(Executor executor) {
		this.restService = new RestServiceImpl(executor);
	}
	
	private final RestServiceImpl restService;
	
}
