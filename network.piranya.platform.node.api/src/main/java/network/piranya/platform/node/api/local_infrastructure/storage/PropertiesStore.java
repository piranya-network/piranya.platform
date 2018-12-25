package network.piranya.platform.node.api.local_infrastructure.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import network.piranya.platform.api.exceptions.PiranyaException;
import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.node.utilities.StringUtils;

public class PropertiesStore {
	
	public Properties properties() {
		return this.properties;
	}
	
	public void update() {
		try (FileOutputStream fileOut = new FileOutputStream(this.propertiesFile)) {
			this.properties.store(fileOut, null);
		} catch (Throwable ex) {
			throw new PiranyaException(ex);
		}
	}
	
	public Optional<String> get(String key) {
		String value = properties().getProperty(key);
		return value != null ? Optional.of(value) : Optional.empty();
	}
	
	public void updateProperty(String key, String newValue) {
		this.properties.put(key, newValue);
		update();
	}
	
	public Map<String, String> getMap(String key) {
		Optional<String> value = get(key);
		if (value.isPresent()) {
			try {
				String[] entries = StringUtils.splitString(value.get(), ',');
				Map<String, String> result = new HashMap<>();
				for (String entry : entries) {
					if (StringUtils.hasText(entry)) {
						String[] parts = StringUtils.splitString(entry, '=');
						result.put(parts[0].trim(), parts[1].trim());
					}
				}
				return result;
			} catch (Throwable ex) {
				throw new PiranyaException(String.format("Failed to parse entry '%s': %s", value.get(), ex.getMessage()));
			}
		} else {
			return new HashMap<>();
		}
	}
	
	public void updateProperty(String key, Map<String, String> map) {
		List<String> items = new ArrayList<>();
		for (Map.Entry<String, String> entry : map.entrySet()) {
			items.add(String.format("%s%s%s", entry.getKey(), '=', entry.getValue()));
		}
		
		updateProperty(key, StringUtils.concatStringCollection(items, ",", false));
	}
	
	
	public PropertiesStore(File propertiesFile) {
		this.properties = new Properties();
		this.propertiesFile = propertiesFile;
		
		try {
			if (propertiesFile.exists()) {
				try (FileInputStream fileIn = new FileInputStream(propertiesFile)) {
					this.properties.load(fileIn);
				}
			}
		} catch (Throwable ex) {
			throw new PiranyaException(ex);
		}
	}
	
	private final Properties properties;
	private final File propertiesFile;
	
}
