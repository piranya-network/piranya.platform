package network.piranya.platform.node.core.execution.engine;

import network.piranya.platform.node.core.execution.engine.activity_log.EsLog;

public abstract class ExternalExecutionBranch {
	
	public void acceptActivityLogEntry(EsLog log) {
		
	}
	
	public void startReplay() {
		setReplaying(true);
	}
	
	public void finishReplay() {
		setReplaying(false);
	}
	
	
	public ExternalExecutionBranch(String branchId) {
		this.branchId = branchId;
	}
	
	private final String branchId;
	protected String branchId() {
		return branchId;
	}
	
	private boolean isReplaying = false;
	protected boolean isReplaying() {
		return isReplaying;
	}
	protected void setReplaying(boolean isReplaying) {
		this.isReplaying = isReplaying;
	}
	
}
