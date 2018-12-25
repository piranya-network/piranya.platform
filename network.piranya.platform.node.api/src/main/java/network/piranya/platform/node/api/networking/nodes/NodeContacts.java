package network.piranya.platform.node.api.networking.nodes;

import network.piranya.platform.api.accounting.AccountRef;
import network.piranya.platform.api.accounting.NodeId;

public class NodeContacts {
	
	private final NodeId nodeId;
	public NodeId nodeId() {
		return nodeId;
	}
	
	private final String host;
	public String host() {
		return host;
	}
	
	private final int publicPort;
	public int publicPort() {
		return publicPort;
	}
	
	private final AccountRef accountId;
	public AccountRef accountId() {
		return accountId;
	}
	
	private final Zone zone;
	public Zone zone() {
		return zone;
	}
	
	public NodeContacts(NodeId nodeId, String host, int publicPort, AccountRef accountId, Zone zone) {
		this.nodeId = nodeId;
		this.host = host;
		this.publicPort = publicPort;
		this.accountId = accountId;
		this.zone = zone;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof NodeContacts) {
			NodeContacts o = (NodeContacts)obj;
			return nodeId().equals(o.nodeId()) && accountId().equals(o.accountId())
					&& host().equals(o.host()) && publicPort() == o.publicPort() && zone().equals(o.zone());
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return nodeId().hashCode();
	}
	
}
