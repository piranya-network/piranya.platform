package network.piranya.platform.node.core.execution.testing.utils;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;

public class HttpServerUtils implements AutoCloseable {
	
	public String serveJsonFile(String url, String json) {
		Router router = Router.router(vertx);
		router.get(url).produces("application/json").handler(request -> request.response().end(json));
		server.requestHandler(router::accept);
		server.listen(++port, "127.0.0.1");
		return String.format("http://127.0.0.1:%s/%s", port, url.indexOf('/') == 0 ? url.substring(1) : url);
	}
	
	private static Vertx vertx = Vertx.vertx();
	
	public HttpServerUtils() {
		this.server = vertx.createHttpServer();
	}
	
	private static int port = 5400;
	
	private final HttpServer server;

	@Override
	public void close() {
		server.close();
	}
	
}
