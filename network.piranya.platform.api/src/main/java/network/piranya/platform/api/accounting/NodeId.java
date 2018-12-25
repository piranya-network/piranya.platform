package network.piranya.platform.api.accounting;

public class NodeId {
	
	private final String id;
	public String id() {
		return id;
	}
	
	private final String machineId;
	public String machineId() {
		return machineId;
	}
	
	private final int localIndex;
	public int localIndex() {
		return localIndex;
	}
	
	public NodeId(String machineId, int localIndex) {
		this.machineId = machineId;
		this.localIndex = localIndex;
		this.id = String.format("%s:%s", machineId, localIndex);
	}
	
}
