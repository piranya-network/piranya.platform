package network.piranya.platform.node.api.networking.nodes;

public class NetworkAddress {
	
	private final String host;
	public String host() {
		return host;
	}
	
	private final int port;
	public int port() {
		return port;
	}
	
	public NetworkAddress(String host, int port) {
		this.host = host;
		this.port = port;
	}
}