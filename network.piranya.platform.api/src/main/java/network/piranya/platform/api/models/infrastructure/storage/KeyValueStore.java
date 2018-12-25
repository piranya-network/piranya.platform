package network.piranya.platform.api.models.infrastructure.storage;

import network.piranya.platform.api.lang.Optional;
import java.util.function.BiConsumer;

public interface KeyValueStore {
	
	Optional<String> get(String key);
	void put(String key, String value);
	void delete(String key);
	void iterate(String keyPrefix, BiConsumer<String, String> consumer);
	
}
