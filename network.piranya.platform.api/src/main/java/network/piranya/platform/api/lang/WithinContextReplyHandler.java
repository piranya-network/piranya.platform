package network.piranya.platform.api.lang;

import java.util.function.BiConsumer;

public interface WithinContextReplyHandler<Reply, Context> {
	
	public WithinContextReplyHandler<Reply, Context> onReply(BiConsumer<Reply, Context> replyHandler);
	
	public WithinContextReplyHandler<Reply, Context> onError(BiConsumer<Exception, Context> errorHandler);
	
	public WithinContextReplyHandler<Reply, Context> onTimeout(BiConsumer<Void, Context> timeoutHandler);
	
}
