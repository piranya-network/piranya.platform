package network.piranya.platform.tests.commons;

import network.piranya.platform.api.lang.ReplyHandler;
import network.piranya.platform.node.utilities.RefImpl;
import network.piranya.platform.node.utilities.TimeService;
import network.piranya.platform.tests.commons.node.NodeBuilder;
import network.piranya.platform.tests.commons.node.TestAccountGenerator;

public abstract class AbstractPlatformTest {
	
	protected <T> T await(ReplyHandler<T> replyHandler, int timeout) {
		RefImpl<T> replyRef = new RefImpl<>();
		RefImpl<Exception> errorRef = new RefImpl<>();
		replyHandler.onReply(reply -> replyRef.set(reply));
		replyHandler.onError(error -> {
			System.out.println("error: " + error.getMessage());
			errorRef.set(error);});

		long startTime = now();
		while (now() - startTime < timeout && replyRef.isEmpty() && errorRef.isEmpty()) {
			try { Thread.sleep(5); } catch (Throwable ex) { throw new RuntimeException(ex); }
		}
		
		if (!replyRef.isEmpty()) {
			return replyRef.get();
		} else if (!errorRef.isEmpty()) {
			throw new RuntimeException(errorRef.get());
		} else {
			throw new RuntimeException("Timeout");
		}
	}
	
	protected long now() {
		return TimeService.now();
	}
	
	
	protected NodeBuilder newNodeBuilder() {
		return new NodeBuilder();
	}
	
	private final TestAccountGenerator accountGenerator = new TestAccountGenerator();
	protected TestAccountGenerator accountGenerator() {
		return accountGenerator;
	}
	
}
