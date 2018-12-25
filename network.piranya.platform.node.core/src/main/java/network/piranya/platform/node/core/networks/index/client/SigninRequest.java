package network.piranya.platform.node.core.networks.index.client;

import network.piranya.platform.api.accounting.NetworkCredentials;
import network.piranya.platform.node.api.networking.nodes.Message;
import network.piranya.platform.node.api.networking.nodes.NodeContacts;

public class SigninRequest extends Message {
	
	public SigninRequest(NetworkCredentials credentials, NodeContacts senderContacts) {
		this.credentials = credentials;
		this.senderContacts = senderContacts;
	}
	
	private final NetworkCredentials credentials;
	public NetworkCredentials credentials() {
		return credentials;
	}
	
	private final NodeContacts senderContacts;
	public NodeContacts senderContacts() {
		return senderContacts;
	}
	
}
