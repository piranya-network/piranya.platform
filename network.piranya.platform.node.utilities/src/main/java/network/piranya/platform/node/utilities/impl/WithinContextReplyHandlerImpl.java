package network.piranya.platform.node.utilities.impl;

import java.util.function.BiConsumer;

import network.piranya.platform.api.exceptions.PiranyaException;
import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.api.lang.WithinContextReplyHandler;

public class WithinContextReplyHandlerImpl<Reply, Context> implements WithinContextReplyHandler<Reply, Context> {
	
	@Override
	public WithinContextReplyHandler<Reply, Context> onReply(BiConsumer<Reply, Context> replyHandler) {
		this.replyHandler = Optional.of(replyHandler);
		this.reply.ifPresent(reply -> replyHandler.accept(reply, context));
		return this;
	}
	private Optional<BiConsumer<Reply, Context>> replyHandler = Optional.empty();
	
	@Override
	public WithinContextReplyHandler<Reply, Context> onError(BiConsumer<Exception, Context> errorHandler) {
		this.errorHandler = Optional.of(errorHandler);
		this.error.ifPresent(error -> errorHandler.accept(error, context));
		return this;
	}
	private Optional<BiConsumer<Exception, Context>> errorHandler = Optional.empty();
	
	@Override
	public WithinContextReplyHandler<Reply, Context> onTimeout(BiConsumer<Void, Context> timeoutHandler) {
		this.timeoutHandler = Optional.of(timeoutHandler);
		this.timeout.ifPresent(timeout -> timeoutHandler.accept(null, context));
		return this;
	}
	private Optional<BiConsumer<Void, Context>> timeoutHandler = Optional.empty();
	
	
	public void doReply(Reply reply, Context context) {
		this.reply = Optional.of(reply);
		this.context = context;
		this.replyHandler.ifPresent(handler -> handler.accept(reply, context));
	}
	private Optional<Reply> reply = Optional.empty();
	
	public void doError(Exception ex, Context context) {
		this.error = Optional.of(ex);
		this.context = context;
		this.errorHandler.ifPresent(handler -> handler.accept(ex, context));
	}
	private Optional<Exception> error = Optional.empty();
	
	public void doTimeout(Context context) {
		this.timeout = Optional.of(true);
		this.context = context;
		this.timeoutHandler.ifPresent(handler -> handler.accept(null, context));
		this.errorHandler.ifPresent(handler -> handler.accept(new PiranyaException("Time out"), context));
	}
	private Optional<Boolean> timeout = Optional.empty();
	
	private Context context;
	
}
