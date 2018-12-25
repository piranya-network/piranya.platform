package network.piranya.platform.node.core.networks.index.client.messages;

import network.piranya.platform.node.api.networking.nodes.Message;

public class RecordFillMessage extends Message {
	
	private final long fillVolume;
	public long fillVolume() {
		return fillVolume;
	}
	
	public RecordFillMessage(long fillVolume) {
		this.fillVolume = fillVolume;
	}
	
}
