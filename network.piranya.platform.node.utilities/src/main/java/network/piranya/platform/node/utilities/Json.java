package network.piranya.platform.node.utilities;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import network.piranya.platform.api.exceptions.EncodingException;

public class Json {
	
	public static String toJsonString(Object obj) {
		try {
			return objectMapper.writeValueAsString(obj);
		} catch (JsonProcessingException ex) {
			throw new EncodingException(ex);
		}
	}
	
	public static <T> T fromJson(String json, Class<T> valueType) {
		try {
			return objectMapper.readValue(json, valueType);
		} catch (IOException ex) {
			throw new EncodingException(ex);
		}
	}
	
	public static <T> T fromJson(InputStream jsonIn, Class<T> valueType) {
		try {
			return objectMapper.readValue(jsonIn, valueType);
		} catch (IOException ex) {
			throw new EncodingException(ex);
		}
	}
	
	
	private static final ObjectMapper objectMapper = new ObjectMapper();
	static {
		objectMapper.registerModule(new Jdk8Module());
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		objectMapper.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
	}
	
}
