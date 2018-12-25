package network.piranya.platform.node.api.app.services;

import network.piranya.platform.api.extension_models.app.AppService;
import network.piranya.platform.api.lang.ResultHandler;

public interface AppServicesRegistry {
	
	<ServiceType extends AppService> void register(Class<ServiceType> appServiceType);
	<ServiceType extends AppService> void deregister(Class<ServiceType> appServiceType);
	
	OperationInvoker invoker(String serviceId, String operationId);
	
	
	public interface OperationInvoker {
		
		void invoke(Object request, ResultHandler<Object> resultHandler);
		
		Class<?> requestType();
	}
	
}
