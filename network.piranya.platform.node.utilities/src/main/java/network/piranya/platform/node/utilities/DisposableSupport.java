package network.piranya.platform.node.utilities;

import network.piranya.platform.api.exceptions.PiranyaException;

public class DisposableSupport {
	
	public void checkIfDisposed() {
		if (disposed) {
			throw new PiranyaException(String.format("%s is disposed", disposable.getClass().getSimpleName()));
		}
	}
	
	public void markDisposed() {
		checkIfDisposed();
		this.disposed = true;
	}
	
	public boolean isDisposed() { return disposed; }
	
	
	public DisposableSupport(Object disposable) {
		this.disposable = disposable;
		
	}
	
	private final Object disposable;	
	private boolean disposed = false;
}
