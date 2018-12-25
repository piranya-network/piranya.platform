package network.piranya.platform.node.core.execution.engine.infrastructure.storage;

import java.io.DataOutput;
import java.io.IOException;
import java.util.function.Consumer;

import network.piranya.platform.api.lang.DisposableIterator;
import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.api.models.infrastructure.storage.QueueStore;
import network.piranya.platform.node.utilities.DisposableSupport;
import network.piranya.platform.node.utilities.TimeService;

public class QueueStoreImpl implements QueueStore {
	
	@Override
	public void append(Object entry) {
		disposable.checkIfDisposed();
		
		branch().append(entry);
	}
	
	@Override
	public void read(Consumer<Object> processor) {
		disposable.checkIfDisposed();
		
		branch().read(processor, Long.MIN_VALUE, Long.MAX_VALUE, Optional.empty());
	}
	
	@Override
	public void read(long startTime, long endTime, Consumer<Object> processor) {
		disposable.checkIfDisposed();
		
		branch().read(processor, startTime, endTime, Optional.empty());
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <EntryType> void read(Class<EntryType> entryType, Consumer<EntryType> processor) {
		disposable.checkIfDisposed();
		
		branch().read((Consumer<Object>)processor, Long.MIN_VALUE, Long.MAX_VALUE, Optional.of(branch().encoder().getDataTypeId(entryType)));
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <EntryType> void read(long startTime, long endTime, Class<EntryType> entryType, Consumer<EntryType> processor) {
		disposable.checkIfDisposed();
		
		branch().read((Consumer<Object>)processor, startTime, endTime, Optional.of(branch().encoder().getDataTypeId(entryType)));
	}
	
	@Override
	public DisposableIterator<Object> read() {
		return branch().read();
	}
	
	public void dispose() {
		disposable.markDisposed();
		branch.unuse(this);
	}
	
	@Override
	protected void finalize() throws Throwable {
		if (!disposable.isDisposed()) {
			dispose();
		}
	}
	
	
	protected void prependTime(DataOutput output) {
		try { output.writeLong(TimeService.now()); }
		catch (IOException ex) { throw new RuntimeException(ex); }
	}
	
	
	public QueueStoreImpl(QueueStoreBranch branch) {
		this.branch = branch;
		branch.use(this);
	}
	
	private final QueueStoreBranch branch;
	public QueueStoreBranch branch() { return branch; }
	
	private final DisposableSupport disposable = new DisposableSupport(this);
	
}
