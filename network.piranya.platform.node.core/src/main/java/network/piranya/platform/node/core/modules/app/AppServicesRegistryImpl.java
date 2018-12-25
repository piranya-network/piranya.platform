package network.piranya.platform.node.core.modules.app;

import static network.piranya.platform.node.utilities.ReflectionUtils.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import network.piranya.platform.api.exceptions.PiranyaException;
import network.piranya.platform.api.extension_models.app.AppService;
import network.piranya.platform.api.extension_models.app.Operation;
import network.piranya.platform.api.lang.ResultHandler;
import network.piranya.platform.api.extension_models.app.AppServiceMetadata;
import network.piranya.platform.node.api.app.services.AppServicesRegistry;
import network.piranya.platform.node.api.execution.ExecutionManager;
import network.piranya.platform.node.api.execution.bots.BotsRegistry;
import network.piranya.platform.node.api.execution.liquidity.LiquidityProvidersRegistry;
import network.piranya.platform.node.core.execution.bots.BotsRegistryImpl;
import network.piranya.platform.node.core.execution.context.ManagerialExtensionContextImpl;

public class AppServicesRegistryImpl implements AppServicesRegistry {
	
	@Override
	@SuppressWarnings("unchecked")
	public <ServiceType extends AppService> void register(Class<ServiceType> appServiceType) {
		try {
			if (!appServiceType.isAnnotationPresent(AppServiceMetadata.class)) {
				throw new PiranyaException(String.format("Class '%s' does not have the '%s' annotation", appServiceType.getName(), AppServiceMetadata.class.getName()));
			}
			
			AppService serviceInstance = createInstance(appServiceType);
			Supplier<AppService> serviceInstanceSupplier = () -> serviceInstance;
			
			Map<String, OperationInvokerImpl> invokers = new HashMap<>();
			for (Method method : appServiceType.getDeclaredMethods()) {
				if (method.isAnnotationPresent(Operation.class)) {
					if (!Modifier.isPublic(method.getModifiers())) {
						throw new PiranyaException(String.format("Operation method '%s' must be public", method.getName()));
					}
					if (method.getParameterTypes().length != 1 && method.getParameterTypes().length != 2) {
						throw new PiranyaException(String.format("App Service Operation Method '%s' must have either 1 or 2 arguments maximum."
								+ " First is request object, the second is a result handler that implements ResultHandler", method.getName()));
					}
					if (method.getParameterTypes().length == 2 && !ResultHandler.class.isAssignableFrom(method.getParameterTypes()[1])) {
						throw new PiranyaException(String.format("Second argument of method '%s' must be a result handler that implements ResultHandler<>", method.getName()));
					}
					
					invokers.put(method.getAnnotation(Operation.class).value(), new OperationInvokerImpl(
							method.getParameterTypes()[0], method, method.getParameterTypes().length == 2, serviceInstanceSupplier));
				}
			}
			
			if (invokers.isEmpty()) {
				throw new PiranyaException(String.format("No operations were located on App Service class '%s'", appServiceType.getName()));
			}
			
			inject(serviceInstance, AppService.class, "context", new ManagerialExtensionContextImpl(
					executionManager().createDetachedExtensionContext(moduleId((Class<AppService>)appServiceType)), liquidityProvidersRegistry()));
			inject(serviceInstance, AppService.class, "appEventsPublisher", appEventsPublisher());
			
			serviceDetailsMap().put(/*appServiceType.getAnnotation(AppServiceMetadata.class).id()*/appServiceType.getName(), new ServiceDetails(invokers));
		} catch (PiranyaException ex) {
			throw ex;
		} catch (Throwable ex) {
			throw new PiranyaException(String.format("Failed to register App Service class '%s': %s", appServiceType.getName(), ex.getMessage()), ex);
		}
	}
	
	protected String moduleId(Class<AppService> appServiceType) {
		// TODO not used for now
		return null;
	}
	
	@Override
	public <ServiceType extends AppService> void deregister(Class<ServiceType> appServiceType) {
		if (appServiceType.isAnnotationPresent(AppServiceMetadata.class)) {
			serviceDetailsMap().remove(appServiceType.getAnnotation(AppServiceMetadata.class).id());
		}
	}
	
	@Override
	public OperationInvoker invoker(String serviceId, String operationId) {
		ServiceDetails serviceDetails = serviceDetailsMap().get(serviceId);
		if (serviceDetails == null) {
			throw new PiranyaException(String.format("App Service '%s' was not found", serviceId));
		}
		
		OperationInvokerImpl invoker = serviceDetails.invokers().get(operationId);
		if (invoker == null) {
			throw new PiranyaException(String.format("Operation '%s' of App Service '%s' was not found", operationId, serviceId));
		}
		
		return invoker;
	}
	
	
	public AppServicesRegistryImpl(BotsRegistryImpl botsRegistry, LiquidityProvidersRegistry liquidityProvidersRegistry, ExecutionManager executionManager,
			BiConsumer<String, Object> appEventsPublisher) {
		this.botsRegistry = botsRegistry;
		this.liquidityProvidersRegistry = liquidityProvidersRegistry;
		this.executionManager = executionManager;
		this.appEventsPublisher = appEventsPublisher;
	}
	
	private final BotsRegistry botsRegistry;
	protected BotsRegistry botsRegistry() { return botsRegistry; }
	
	private final LiquidityProvidersRegistry liquidityProvidersRegistry;
	protected LiquidityProvidersRegistry liquidityProvidersRegistry() { return liquidityProvidersRegistry; }
	
	private final ExecutionManager executionManager;
	protected ExecutionManager executionManager() { return executionManager; }
	
	private final ConcurrentMap<String, ServiceDetails> serviceDetailsMap = new ConcurrentHashMap<>();
	protected ConcurrentMap<String, ServiceDetails> serviceDetailsMap() { return serviceDetailsMap; }
	
	private final BiConsumer<String, Object> appEventsPublisher;
	protected BiConsumer<String, Object> appEventsPublisher() { return appEventsPublisher; }
	
	
	protected class ServiceDetails {
		
		private final Map<String, OperationInvokerImpl> invokers;
		public Map<String, OperationInvokerImpl> invokers() {
			return invokers;
		}
		
		public ServiceDetails(Map<String, OperationInvokerImpl> invokers) {
			this.invokers = invokers;
		}
	}
	
	protected class OperationInvokerImpl implements OperationInvoker {
		
		@Override
		public void invoke(Object request, ResultHandler<Object> resultHandler) {
			try {
				if (acceptsResultHandler) {
					method.invoke(serviceInstanceSupplier.get(), request, resultHandler);
				} else {
					method.invoke(serviceInstanceSupplier.get(), request);
				}
			} catch (Throwable ex) {
				throw new PiranyaException(String.format("An error occurred while invoking '%s.%s': %s",
						method.getDeclaringClass().getName(), method.getName(), ex.getMessage()), ex);
			}
		}
		
		@Override
		public Class<?> requestType() {
			return requestType;
		}
		
		
		public OperationInvokerImpl(Class<?> requestType, Method method, boolean acceptsResultHandler, Supplier<AppService> serviceInstanceSupplier) {
			this.requestType = requestType;
			this.method = method;
			this.acceptsResultHandler = acceptsResultHandler;
			this.serviceInstanceSupplier = serviceInstanceSupplier;
		}
		
		private final Class<?> requestType;
		private final Method method;
		private final boolean acceptsResultHandler;
		private Supplier<AppService> serviceInstanceSupplier;
	}
	
}
