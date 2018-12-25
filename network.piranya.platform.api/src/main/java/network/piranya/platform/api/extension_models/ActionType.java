package network.piranya.platform.api.extension_models;

public enum ActionType {
	
	RUN_BOT("run"), GOTO_PAGE("page"), OPEN_WEB_SITE("website"), EXPAND("expand"), UI_ACTION("ui_action"), REGISTER_LP("register_lp");
	
	private ActionType(String prefix) {
		this.prefix = prefix;
	}
	
	private final String prefix;
	public String prefix() { return prefix; }
	
}
