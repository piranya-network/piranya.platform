package network.piranya.platform.node.core.networks.index.client.messages;

import network.piranya.platform.node.api.networking.nodes.Message;

public class Pong extends Message {
	
	public boolean includesInstruction(int instruction) {
		return (instructions() & instruction) != 0;
	}
	
	
	private int instructions;
	public int instructions() {
		return instructions;
	}
	
	public Pong() {
		this(0);
	}
	
	public Pong(int instructions) {
		this.instructions = instructions;
	}
	
	
	//public static final short REFRESH_ZONE = 1 << 2;
	
}
