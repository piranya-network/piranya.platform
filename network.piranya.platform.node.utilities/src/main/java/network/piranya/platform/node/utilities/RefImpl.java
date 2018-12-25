package network.piranya.platform.node.utilities;

public class RefImpl<T> implements Ref<T> {
	
	public T get() {
		return value;
	}
	
	public boolean isEmpty() {
		return get() == null;
	}
	
	public void set(T value) {
		this.value = value;
	}
	
	private T value;
	
}
