package network.piranya.platform.node.core.execution.engine.infrastructure.storage;

import java.util.function.BiConsumer;

import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.api.models.infrastructure.storage.KeyValueStore;
import network.piranya.platform.node.utilities.DisposableSupport;

public class KeyValueStoreImpl implements KeyValueStore {
	
	@Override
	public Optional<String> get(String key) {
		disposable.checkIfDisposed();
		
		return branch().get(key);
	}
	
	@Override
	public void put(String key, String value) {
		disposable.checkIfDisposed();
		
		branch().put(key, value);
	}
	
	@Override
	public void delete(String key) {
		disposable.checkIfDisposed();
		
		branch().delete(key);
	}
	
	@Override
	public void iterate(String keyPrefix, BiConsumer<String, String> consumer) {
		disposable.checkIfDisposed();
		
		branch().iterate(keyPrefix, consumer);
	}
	
	public void dispose() {
		disposable.markDisposed();
	}
	
	@Override
	protected void finalize() throws Throwable {
		if (!disposable.isDisposed()) {
			dispose();
		}
	}
	
	
	public KeyValueStoreImpl(KeyValueStoreBranch branch) {
		this.branch = branch;
	}
	
	private final KeyValueStoreBranch branch;
	public KeyValueStoreBranch branch() { return branch; }
	
	private final DisposableSupport disposable = new DisposableSupport(this);
	
}
