package network.piranya.platform.node.core.networks.index.client.messages;

import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.node.api.networking.nodes.Message;
import network.piranya.platform.node.api.networking.nodes.NodeContacts;

public class Ping extends Message {
	
	public boolean hasInstruction(int instruction) {
		return (instructions() & instruction) != 0;
	}
	
	public boolean hasFlag(int flag) {
		return (flags() & flag) != 0;
	}
	
	private NodeContacts senderContacts;
	public NodeContacts senderContacts() {
		return senderContacts;
	}
	public void setSenderContacts(NodeContacts senderContacts) {
		this.senderContacts = senderContacts;
	}
	
	
	private final int instructions;
	public int instructions() {
		return instructions;
	}
	
	private final int flags;
	public int flags() {
		return flags;
	}
	
	private final Optional<Long> accountingCycleTime;
	public Optional<Long> accountingCycleTime() {
		return accountingCycleTime;
	}
	
	public Ping() {
		this(0);
	}
	
	public Ping(int instructions) {
		this(instructions, 0, Optional.empty());
	}
	
	public Ping(int instructions, int flags, Optional<Long> accountingCycleTime) {
		this.instructions = instructions;
		this.flags = flags;
		this.accountingCycleTime = accountingCycleTime;
	}
	
	
	public static final short UPDATE_ZONE_INSTRUCTION = 1 << 2;
	public static final short CHECK_LATENCY_INSTRUCTION = 1 << 3;
	
	public static final short NEW_ACCOUNTING_CLUSTER_AWARE_FLAG = 1 << 2;
	
}
