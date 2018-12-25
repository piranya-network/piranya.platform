package network.piranya.platform.node.app.http;

import static network.piranya.platform.node.utilities.CollectionUtils.map;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;
import network.piranya.infrastructure.dcm4j.api.ComponentModel;
import network.piranya.platform.api.accounting.NetworkCredentials;
import network.piranya.platform.api.exceptions.InvalidParameterException;
import network.piranya.platform.api.exceptions.PiranyaException;
import network.piranya.platform.api.extension_models.Data;
import network.piranya.platform.api.extension_models.Parameters;
import network.piranya.platform.api.lang.None;
import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.api.models.bots.BotRef;
import network.piranya.platform.api.models.bots.BotSpec;
import network.piranya.platform.node.api.app.services.AppServicesRegistry;
import network.piranya.platform.node.api.app.services.AppServicesRegistry.OperationInvoker;
import network.piranya.platform.node.api.booting.NetworkNodeConfig;
import network.piranya.platform.node.api.execution.ExecutionManager;
import network.piranya.platform.node.api.execution.bots.BotsRegistry;
import network.piranya.platform.node.api.execution.commands.InvokeCommandBot;
import network.piranya.platform.node.api.execution.commands.InvokeExecutionBotCommand;
import network.piranya.platform.node.core.booting.NetworkNode;
import network.piranya.platform.node.core.local_infrastructure.LocalServicesProviderImpl;
import network.piranya.platform.node.utilities.StringUtils;
import network.piranya.platform.node.utilities.TimeService;

public class AppHttpServer {
	
	protected void init() {
		prepareAppDir();
		
		Router router = Router.router(vertx());
		router.route(/*"/api/login"*/).handler(BodyHandler.create().setMergeFormAttributes(true));
		router.route().handler(CorsHandler.create("*").allowedMethod(HttpMethod.GET).allowedMethod(HttpMethod.POST)
				.allowedMethod(HttpMethod.PUT).allowedMethod(HttpMethod.DELETE));
		
		//router.post("/local_services/core_services/submit_password/:owner_id/:service_id/:operation_id").produces("application/json").blockingHandler(this::processOperationRequest);
		router.post("/module_services/app_service/:service_id/:operation_id").produces("application/json").blockingHandler(this::processAppServiceRequest);
		router.post("/module_services/command_bot/:bot_type_id/:command_id").produces("application/json").blockingHandler(this::processCommandBotRequest);
		router.post("/module_services/execution_bot/:bot_ref/:command_id").produces("application/json").blockingHandler(this::processExecutionBotRequest);
		//router.post("/local_services/core_services/:service_id/:operation_id").produces("application/json").handler(this::processOperationRequest);
		//do all through websockets
		// ws.core.loadGeneral
		// ws.page.loadInitial and subscribe to specified
		// TODO go throw signin and loading default page
		
		// ui-structure.js
		
		router.route("/static/*").handler(StaticHandler.create(appStaticContentsDir()).setCachingEnabled(false).setFilesReadOnly(false));
		
		viewsHttpAppManager().init(router);
		
		server().requestHandler(router::accept);
		
		server().websocketHandler(webEventsPublisher().websocketHandler());
		
		server().listen(config().httpPort(), "0.0.0.0");
		
		signin(null);
	}
	
	public void signin(NetworkCredentials credentials) {
		try {
			NetworkNode node = new NetworkNode(credentials, config(), new LocalServicesProviderImpl(config(), credentials), componentModel(),
					(eventType, data) -> webEventsPublisher().publishEvent(new WebEvent(eventType, data)));
			node.boot();
			setNode(Optional.of(node));
		} catch (Throwable ex) {
			LOG.error(String.format("Failed to process signin: %s", ex.getMessage()), ex);
		}
	}
	
	protected void processAppServiceRequest(RoutingContext request) {
		try {
			System.out.println("processAppServiceRequest");
			String serviceId = request.request().getParam("service_id");
			String operationId = request.request().getParam("operation_id");
			OperationInvoker invoker = servicesRegistry().invoker(serviceId, operationId);
			
			invoker.invoke(decodeRequest(request.getBodyAsString(), invoker.requestType()), result -> {
				request.response().putHeader("content-type", "application/json; charset=utf-8").end(result.isSuccessful()
						? encodeReply(result.result().get()) : encodeError(result.error().get()));
			});
		} catch (Throwable ex) {
			LOG.warn(String.format("Failed to process request '%s': %s", request.request().path(), ex.getMessage()), ex);
		}
	}
	
	protected void processCommandBotRequest(RoutingContext request) {
		try {
			System.out.println("processCommandBotRequest");
			String botTypeId = request.request().getParam("bot_type_id");
			String commandId = request.request().getParam("command_id");
			
			executionManager().execute(new InvokeCommandBot(BotSpec.byType(botTypeId), commandId, decodeRequest(request.getBodyAsString(), Parameters.class),
					result -> request.response().putHeader("content-type", "application/json; charset=utf-8").end(result.isSuccessful()
							? encodeReply(result.result().get()) : encodeError(result.error().get()))
			));
		} catch (Throwable ex) {
			LOG.warn(String.format("Failed to process request '%s': %s", request.request().path(), ex.getMessage()), ex);
		}
	}
	
	protected void processAction(String actionDescriptor) {
		if (actionDescriptor.contains(".")) {
			// command
		} else if (actionDescriptor.contains("!")) {
			// execution bot
		} else {
			// feature: search in command bots and their commands and in execution bots class features
		}
	}
	
	protected void processExecutionBotRequest(RoutingContext request) {
		try {
			BotRef botRef = new BotRef(request.request().getParam("bot_ref"));
			String commandId = request.request().getParam("command_id");
			
			executionManager().execute(new InvokeExecutionBotCommand(botRef, commandId, decodeRequest(request.getBodyAsString(), Parameters.class),
					result -> request.response().putHeader("content-type", "application/json; charset=utf-8").end(result.isSuccessful()
							? encodeReply(result.result().get()) : encodeError(result.error().get()))
			));
		} catch (Throwable ex) {
			LOG.warn(String.format("Failed to process request '%s': %s", request.request().path(), ex.getMessage()), ex);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected <RequestType> RequestType decodeRequest(String json, Class<RequestType> expectedType) {
		if (Parameters.class.isAssignableFrom(expectedType)) {
			JsonObject jsonObj = new JsonObject(json);
			Iterator<Entry<String, Object>> iterator = jsonObj.iterator();
			Map<String, Object> data = new HashMap<>();
			while (iterator.hasNext()) {
				Entry<String, Object> entry = iterator.next();
				if (entry.getValue() != null) {
					if (entry.getValue() instanceof JsonArray) {
						data.put(entry.getKey(), map(((JsonArray)entry.getValue()).getList(), o -> o.toString()));
					} else {
						data.put(entry.getKey(), entry.getValue().toString());
					}
				}
			}
			return (RequestType)new Parameters(data);
		} else {
			return Json.decodeValue(json, expectedType);
		}
	}
	
	protected String encodeReply(Object reply) {
		if (reply instanceof Data) {
			return Json.encodePrettily(((Data)reply).dataMap());
		} else if (reply instanceof None) {
			return "{}";
		} else {
			return Json.encodePrettily(reply);
		}
	}
	
	protected String encodeError(Exception ex) {
		JsonObject jsonObj = new JsonObject();
		jsonObj.put("error", ex.getMessage() != null ? ex.getMessage() : ("Error: " + ex.getClass().getName()));
		if (ex instanceof InvalidParameterException) {
			jsonObj.put("parameter_id", ((InvalidParameterException)ex).getParameterId());
		}
		return jsonObj.encode();
	}
	
	public void dispose() {
		try { server().close(); } catch (Throwable ex) { }
	}
	
	protected void prepareAppDir() {
		try {
			File indexFile = new File(appStaticContentsDir(), "index-dev.html");
			File actualIndexFile = new File(appStaticContentsDir(), "index.html");
			
			String replaced = StringUtils.replace(new String(Files.readAllBytes(Paths.get(indexFile.toURI()))), "${random}",  Long.valueOf(TimeService.now() / 1000L).toString());
			Files.write(Paths.get(actualIndexFile.toURI()), replaced.getBytes());
		} catch (Throwable ex) {
			throw new PiranyaException(ex);
		}
	}
	
	
	public AppHttpServer(NetworkNodeConfig config, String appStaticContentsDir, Vertx vertx, ComponentModel componentModel) {
		this.config = config;
		this.vertx = vertx;
		this.appStaticContentsDir = appStaticContentsDir.endsWith("/") || appStaticContentsDir.endsWith("\\") ? appStaticContentsDir : appStaticContentsDir + "/";
		this.componentModel = componentModel;
		this.server = vertx.createHttpServer();
		this.viewsHttpAppManager = new ViewsHttpAppManager(() -> node().get().modulesManager().viewsProvidersRegistry(), webEventsPublisher());
		
		init();
	}
	
	private final NetworkNodeConfig config;
	protected NetworkNodeConfig config() {
		return config;
	}
	
	private final Vertx vertx;
	protected Vertx vertx() {
		return vertx;
	}
	
	private final String appStaticContentsDir;
	protected String appStaticContentsDir() {
		return appStaticContentsDir;
	}
	
	private final HttpServer server;
	protected HttpServer server() {
		return server;
	}
	
	protected AppServicesRegistry servicesRegistry() {
		return node().get().modulesManager().appServicesRegistry();
	}
	
	protected BotsRegistry botsRegistry() {
		return node().get().modulesManager().botsRegistry();
	}
	
	protected ExecutionManager executionManager() {
		return node().get().executionManager();
	}
	
	private final ComponentModel componentModel;
	protected ComponentModel componentModel() {
		return componentModel;
	}
	
	private final WebEventsPublisher webEventsPublisher = new WebEventsPublisher();
	protected WebEventsPublisher webEventsPublisher() {
		return webEventsPublisher;
	}
	
	private final ViewsHttpAppManager viewsHttpAppManager;
	protected ViewsHttpAppManager viewsHttpAppManager() { return viewsHttpAppManager; }
	
	private Optional<NetworkNode> node;
	public Optional<NetworkNode> node() {
		return node;
	}
	protected void setNode(Optional<NetworkNode> node) {
		this.node = node;
	}
	
	
	private static final Logger LOG = LoggerFactory.getLogger(AppHttpServer.class);
	
}
