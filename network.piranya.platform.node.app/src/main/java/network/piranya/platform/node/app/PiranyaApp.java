package network.piranya.platform.node.app;

import io.vertx.core.Vertx;
import network.piranya.infrastructure.dcm4j.api.ComponentModel;
import network.piranya.platform.api.accounting.NetworkCredentials;
import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.node.api.booting.NetworkNodeConfig;
import network.piranya.platform.node.app.http.AppHttpServer;
import network.piranya.platform.node.core.booting.NetworkNode;

public class PiranyaApp {
	
	public void signin(NetworkCredentials credentials) {
		httpServer().signin(credentials);
	}
	
	public Optional<NetworkNode> node() {
		return httpServer().node();
	}
	
	public void dispose() {
		httpServer().dispose();
	}
	
	
	public PiranyaApp(NetworkNodeConfig config, String appStaticContentsDir, ComponentModel componentModel) {
		this.config = config;
		this.httpServer = new AppHttpServer(config, appStaticContentsDir, vertx, componentModel);
	}
	
	private final NetworkNodeConfig config;
	protected NetworkNodeConfig config() {
		return config;
	}
	
	private final AppHttpServer httpServer;
	protected AppHttpServer httpServer() {
		return httpServer;
	}
	
	private static Vertx vertx = Vertx.vertx();
	
}
