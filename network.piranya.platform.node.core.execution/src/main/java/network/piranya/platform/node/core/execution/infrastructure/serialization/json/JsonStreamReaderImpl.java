package network.piranya.platform.node.core.execution.infrastructure.serialization.json;

import java.io.InputStream;
import java.util.function.Consumer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;

import network.piranya.platform.api.exceptions.PiranyaException;
import network.piranya.platform.api.models.infrastructure.net.JsonStreamReader;

public class JsonStreamReaderImpl implements JsonStreamReader {
	
	@Override
	public <Item> void readArray(Class<Item> itemType, Consumer<Item> consumer) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
			
			MappingIterator<Item> items = objectMapper.readValues(parser(), itemType);
			parser().nextToken();
			parser().nextToken();
			
			while (items.hasNext()) {
				consumer.accept(items.next());
			}
		} catch (Exception ex) {
			throw new PiranyaException("JSON parsing error: " + ex.getMessage(), ex);
		}
	}
	
	public JsonStreamReaderImpl(InputStream input) {
		try {
			JsonFactory jsonFactory = new JsonFactory();
			this.parser = jsonFactory.createParser(input);
		} catch (Throwable ex) {
			throw new PiranyaException("JSON parsing error: " + ex.getMessage(), ex);
		}
	}
	
	private final JsonParser parser;
	protected JsonParser parser() { return parser; }
	
}
