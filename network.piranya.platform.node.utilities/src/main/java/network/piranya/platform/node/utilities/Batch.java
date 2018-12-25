package network.piranya.platform.node.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class Batch<Entry> {
	
	public void feed(Entry entry) {
		queue().add(entry);
		
		if (queue().size() >= maxBatchSize()) {
			process();
		}
	}
	
	public void finish() {
		process();
	}
	
	protected void process() {
		if (!queue().isEmpty()) {
			List<Entry> entries = new ArrayList<>();
			while (!queue.isEmpty()) {
				entries.add(queue.poll());
			}
			
			processor.accept(entries);
		}
	}
	
	
	public Batch(int maxBatchSize, Consumer<List<Entry>> processor) {
		this.maxBatchSize = maxBatchSize;
		this.processor = processor;
	}
	
	private final int maxBatchSize;
	protected int maxBatchSize() {
		return maxBatchSize;
	}
	
	private Consumer<List<Entry>> processor;
	protected Consumer<List<Entry>> processor() {
		return processor;
	}
	
	private final ConcurrentLinkedQueue<Entry> queue = new ConcurrentLinkedQueue<>();
	protected ConcurrentLinkedQueue<Entry> queue() {
		return queue;
	}
	
}
