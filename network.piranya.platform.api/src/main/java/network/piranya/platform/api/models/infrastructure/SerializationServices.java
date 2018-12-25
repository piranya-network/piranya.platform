package network.piranya.platform.api.models.infrastructure;

import java.io.InputStream;
import java.util.function.Consumer;

import network.piranya.platform.api.lang.DisposableIterator;

public interface SerializationServices {
	
	String encodeJson(Object obj);
	<ObjectType> ObjectType decodeJson(String json, Class<ObjectType> objectType);
	
	DisposableIterator<String[]> readCsv(InputStream input, CsvReaderParams params);
	void readCsv(InputStream input, CsvReaderParams params, Consumer<String[]> reader);
	
}
