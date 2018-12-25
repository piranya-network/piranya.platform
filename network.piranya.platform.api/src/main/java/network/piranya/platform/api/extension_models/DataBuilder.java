package network.piranya.platform.api.extension_models;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class DataBuilder {
	
	public DataBuilder string(String paramName, String value) {
		params.put(paramName, value);
		return this;
	}
	
	public DataBuilder decimal(String paramName, BigDecimal value) {
		params.put(paramName, value);
		return this;
	}
	
	public DataBuilder bool(String paramName, boolean value) {
		params.put(paramName, value);
		return this;
	}
	
	public Data build() {
		return new Data(params);
	}
	
	private final Map<String, Object> params = new HashMap<>();
	
}
