package network.piranya.platform.api.lang;

import java.util.Iterator;

public interface DisposableIterator<E> extends Iterator<E>, AutoCloseable {
	
	void dispose();
	
}
