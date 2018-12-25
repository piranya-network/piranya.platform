package network.piranya.platform.node.core.local_infrastructure.net;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import network.piranya.platform.api.exceptions.PiranyaException;
import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.node.api.local_infrastructure.concurrency.Executor;
import network.piranya.platform.node.api.networking.nodes.ChunkSender;
import network.piranya.platform.node.api.networking.nodes.Message;

public class ChunkSenderImpl implements ChunkSender {
	
	@Override
	public <Item, ItemMessage extends Message> void iterate(Iterator<Item> itemsIterator, Function<Item, ItemMessage> itemToMessageMapper) {
		iterate(itemsIterator, itemToMessageMapper, () -> Optional.empty());
	}
	
	@Override
	public <Item, ItemMessage extends Message, FinalMessage extends Message> void iterate(Iterator<Item> itemsIterator,
			Function<Item, ItemMessage> itemToMessageMapper, Supplier<Optional<FinalMessage>> finalMessageSupplier) {
		if (isUsed) {
			throw new PiranyaException("Chunk Sender can not be used more than once");
		}
		this.isUsed = true;
		
		context().executionManager().scheduleConveniently(() -> {
			int count = 0;
			ChunkMessage chunkMessage = new ChunkMessage();
			while (itemsIterator.hasNext() && count < CHUNK_LENGTH) {
				chunkMessage.addMessage(itemToMessageMapper.apply(itemsIterator.next()));
			}
			if (!itemsIterator.hasNext()) {
				finalMessageSupplier.get().ifPresent(finalMessage -> chunkMessage.addMessage(finalMessage));
			}
			sender().accept(chunkMessage);
			return itemsIterator.hasNext();
		}, 0, 50, TimeUnit.MICROSECONDS, Executor.LOWEST_PRIORITY);
	}
	
	
	public ChunkSenderImpl(Consumer<Message> sender, ChannelsContext context) {
		this.sender = sender;
		this.context = context;
	}
	
	private final Consumer<Message> sender;
	protected Consumer<Message> sender() {
		return sender;
	}
	
	private final ChannelsContext context;
	protected ChannelsContext context() {
		return context;
	}
	
	private boolean isUsed = false;
	
	
	// TODO optimize - for now simplified to 100 per chunk message, later calculate typical message size from first X messages, and determine chunk size accordingly
	private static final int CHUNK_LENGTH = 100;
	
}
