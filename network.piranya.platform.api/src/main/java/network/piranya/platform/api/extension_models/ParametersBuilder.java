package network.piranya.platform.api.extension_models;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParametersBuilder {
	
	public ParametersBuilder string(String key, String value) {
		params.put(key, value);
		return this;
	}
	
	public ParametersBuilder decimal(String key, BigDecimal value) {
		params.put(key, value);
		return this;
	}
	
	public ParametersBuilder integer(String key, int value) {
		params.put(key, value);
		return this;
	}
	
	public ParametersBuilder longv(String key, long value) {
		params.put(key, value);
		return this;
	}
	
	public ParametersBuilder bool(String key, boolean value) {
		params.put(key, value);
		return this;
	}
	
	public ParametersBuilder from(Parameters other) {
		params.putAll(other.dataMap());
		return this;
	}
	
	public ParametersBuilder stringList(String key, List<String> value) {
		params.put(key, value);
		return this;
	}
	
	public ParametersBuilder merge(Parameters params) {
		return merge(params.dataMap());
	}
	
	public ParametersBuilder merge(Map<String, Object> params) {
		this.params.putAll(params);
		return this;
	}
	
	public Parameters build() {
		return new Parameters(params);
	}
	
	
	public ParametersBuilder() {
		this.params = new HashMap<>();
	}
	
	public ParametersBuilder(Map<String, Object> params) {
		this.params = new HashMap<>(params);
	}
	
	private final Map<String, Object> params;
	
}
