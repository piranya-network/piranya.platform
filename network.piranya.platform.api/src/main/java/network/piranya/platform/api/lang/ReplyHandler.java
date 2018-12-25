package network.piranya.platform.api.lang;

import java.util.function.Consumer;

public interface ReplyHandler<Reply> {
	
	public ReplyHandler<Reply> onReply(Consumer<Reply> replyHandler);
	
	public ReplyHandler<Reply> onError(Consumer<Exception> errorHandler);
	
	public ReplyHandler<Reply> onTimeout(Consumer<Void> timeoutHandler);
	
}
