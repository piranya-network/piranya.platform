package network.piranya.platform.node.app.http;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import network.piranya.platform.api.exceptions.InvalidParameterException;
import network.piranya.platform.api.extension_models.app.ui.ViewsProvider;
import network.piranya.platform.api.extension_models.app.ui.ViewsProvider.ViewEvent;
import network.piranya.platform.node.api.app.ui.ViewsProvidersRegistry;
import network.piranya.platform.node.utilities.Json;

public class ViewsHttpAppManager {
	
	public void init(Router router) {
		router.get("/module_services/views/list/:view_type").produces("application/json").blockingHandler(this::listViews);
		router.post("/module_services/views/watch/:view_type").produces("application/json").blockingHandler(this::watchViews);
		router.post("/module_services/views/unwatch/:view_type").produces("application/json").blockingHandler(this::unwatchViews);
		router.get("/module_services/views/get/:view_type/:view_id").produces("application/json").blockingHandler(this::getView);
	}
	
	protected void watchViews(RoutingContext request) {
		try {
			String viewType = request.request().getParam("view_type");
			List<String> viewsIds = readViewsIds(request);
			
			viewsProvider(viewType).watchViews(viewsIds, result -> request.response().putHeader("content-type", "application/json; charset=utf-8").end(result.isSuccessful()
					? Json.toJsonString(result.result().get()) : encodeError(result.error().get())));
		} catch (Throwable ex) {
			LOG.warn(String.format("Failed to process request '%s': %s", request.request().path(), ex.getMessage()), ex);
		}
	}
	
	protected void unwatchViews(RoutingContext request) {
		
	}
	
	protected void listViews(RoutingContext request) {
		try {
			String viewType = request.request().getParam("view_type");
			System.out.println("listViews: " + viewType);
			
			viewsProvider(viewType).listViews(result -> request.response().putHeader("content-type", "application/json; charset=utf-8").end(Json.toJsonString(result)));
		} catch (Throwable ex) {
			// TODO error
			LOG.warn(String.format("Failed to process request '%s': %s", request.request().path(), ex.getMessage()), ex);
		}
	}
	
	protected void getView(RoutingContext request) {
		try {
			String viewType = request.request().getParam("view_type");
			String viewId = request.request().getParam("view_id");
			
			viewsProvider(viewType).getView(viewId, result -> request.response().putHeader("content-type", "application/json; charset=utf-8").end(result.isSuccessful()
					? Json.toJsonString(result.result().get()) : encodeError(result.error().get())));
		} catch (Throwable ex) {
			LOG.warn(String.format("Failed to process request '%s': %s", request.request().path(), ex.getMessage()), ex);
		}
	}
	
	
	protected void onViewEvent(ViewEvent event) {
		//System.err.println("onViewEvent: " + event.getEventType() + ": " + event.getView().getId());
		webEventsPublisher().publishEvent(new WebEvent("VIEW_EVENT", event));
	}
	
	protected ViewsProvider viewsProvider(String viewType) {
		ViewsProvider viewsProvider = registry().viewsProvider(viewType);
		if (!viewsProvidersSubscribers.containsKey(viewsProvider)) {
			synchronized (viewsProvidersSubscribers) {
				viewsProvider.subscribe(viewsEventsSubscriber);
				viewsProvidersSubscribers.put(viewsProvider, true);
			}
		}
		return viewsProvider;
	}
	

	protected List<String> readViewsIds(RoutingContext request) {
		JsonArray viewsIds = new JsonArray(request.getBodyAsString());
		List<String> result = new ArrayList<>();
		for (Object entry : viewsIds.getList()) {
			result.add(entry.toString());
		}
		return result;
	}
	
	protected String encodeError(Exception ex) {
		JsonObject jsonObj = new JsonObject();
		jsonObj.put("error", ex.getMessage());
		if (ex instanceof InvalidParameterException) {
			jsonObj.put("parameter_id", ((InvalidParameterException)ex).getParameterId());
		}
		return jsonObj.encode();
	}
	
	
	public ViewsHttpAppManager(Supplier<ViewsProvidersRegistry> registry, WebEventsPublisher webEventsPublisher) {
		this.registry = registry;
		this.webEventsPublisher = webEventsPublisher;
	}
	
	private final Supplier<ViewsProvidersRegistry> registry;
	protected ViewsProvidersRegistry registry() { return registry.get(); }
	
	private final WebEventsPublisher webEventsPublisher;
	protected WebEventsPublisher webEventsPublisher() { return webEventsPublisher; }
	
	private final Consumer<ViewEvent> viewsEventsSubscriber = this::onViewEvent;
	
	private final Map<ViewsProvider, Boolean> viewsProvidersSubscribers = new WeakHashMap<>();
	
	
	private static final Logger LOG = LoggerFactory.getLogger(ViewsHttpAppManager.class);
	
}
