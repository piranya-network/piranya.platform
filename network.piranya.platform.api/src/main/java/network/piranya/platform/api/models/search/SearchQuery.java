package network.piranya.platform.api.models.search;

import java.util.HashSet;
import java.util.Set;

public class SearchQuery {
	
	private String queryText;
	public String getQueryText() {
		return queryText;
	}
	public void setQueryText(String queryText) {
		this.queryText = queryText;
	}
	
	private Set<LookIn> lookIn = new HashSet<>();
	public Set<LookIn> getLookIn() {
		return lookIn;
	}
	public void setLookIn(Set<LookIn> lookIn) {
		this.lookIn = lookIn;
	}
	
	private Set<String> features = new HashSet<>();
	public Set<String> getFeatures() {
		return features;
	}
	public void setFeatures(Set<String> features) {
		this.features = features;
	}
	
	
	public SearchQuery() {
	}
	
	public static enum LookIn { COMMAND_BOT_TYPES, EXECUTION_BOT_TYPES, RUNNING_BOTS, ASSETS, INSTRUMENTS, AVAILABLE_LP, REGISTERED_LP, PAGES, UI_ACTIONS }
	
}
