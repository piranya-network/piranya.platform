package network.piranya.platform.node.core.execution.engine.infrastructure.serialization;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;

import network.piranya.platform.api.exceptions.UnexpectedException;
import network.piranya.platform.api.lang.DisposableIterator;
import network.piranya.platform.api.models.infrastructure.CsvReaderParams;
import network.piranya.platform.api.models.infrastructure.SerializationServices;
import network.piranya.platform.node.utilities.Json;

public class SerializationServicesImpl implements SerializationServices {
	
	@Override
	public String encodeJson(Object obj) {
		return Json.toJsonString(obj);
	}
	
	@Override
	public <ObjectType> ObjectType decodeJson(String json, Class<ObjectType> objectType) {
		return Json.fromJson(json, objectType);
	}
	
	public DisposableIterator<String[]> readCsv(InputStream input, CsvReaderParams params) {
		return new CsvIterator(input, params);
	}
	
	@Override
	public void readCsv(InputStream input, CsvReaderParams params, Consumer<String[]> reader) {
		String fieldSplitter = String.format("%s(?=([^%s]*%s[^%s]*%s)*[^%s]*$)",
				params.delimiter(), params.quoteChar(), params.quoteChar(), params.quoteChar(), params.quoteChar(), params.quoteChar());
		
		try (BufferedReader r = new BufferedReader(new InputStreamReader(input))) {
			boolean isFirst = true;
			String line = null;
			while ((line = r.readLine()) != null) {
				if (isFirst && params.isIgnoreFirstRow()) {
					continue;
				}
				isFirst = false;
				
				String[] fields = line.split(fieldSplitter);
				for (int i = 0; i < fields.length; i++) {
					String f = fields[i];
					if (f.charAt(0) == params.quoteChar() && f.charAt(f.length() - 1) == params.quoteChar()) {
						fields[i] = f.substring(1, f.length() - 1);
					}
				}
				
				reader.accept(fields);
			}
		} catch (IOException ex) {
			throw new UnexpectedException(ex);
		}
	}
	
}
