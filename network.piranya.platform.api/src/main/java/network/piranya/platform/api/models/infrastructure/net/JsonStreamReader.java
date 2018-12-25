package network.piranya.platform.api.models.infrastructure.net;

import java.util.function.Consumer;

public interface JsonStreamReader {
	
	<Item> void readArray(Class<Item> itemType, Consumer<Item> consumer);
	
}
