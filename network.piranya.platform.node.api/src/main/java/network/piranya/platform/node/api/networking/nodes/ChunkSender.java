package network.piranya.platform.node.api.networking.nodes;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Supplier;

import network.piranya.platform.api.lang.Optional;

public interface ChunkSender {
	
	<Item, ItemMessage extends Message> void iterate(Iterator<Item> itemsIterator, Function<Item, ItemMessage> itemToMessageMapper);
	
	<Item, ItemMessage extends Message, FinalMessage extends Message> void iterate(
			Iterator<Item> itemsIterator, Function<Item, ItemMessage> itemToMessageMapper, Supplier<Optional<FinalMessage>> finalMessageSupplier);
	
}