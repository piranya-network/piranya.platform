package network.piranya.platform.node.utilities.impl;

import java.util.function.Consumer;

import network.piranya.platform.api.exceptions.PiranyaException;
import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.api.lang.ReplyHandler;

public class ReplyHandlerImpl<Reply> implements ReplyHandler<Reply> {
	
	@Override
	public ReplyHandler<Reply> onReply(Consumer<Reply> replyHandler) {
		this.replyHandler = Optional.of(replyHandler);
		this.reply.ifPresent(reply -> replyHandler.accept(reply));
		return this;
	}
	private Optional<Consumer<Reply>> replyHandler = Optional.empty();
	
	@Override
	public ReplyHandler<Reply> onError(Consumer<Exception> errorHandler) {
		this.errorHandler = Optional.of(errorHandler);
		this.error.ifPresent(error -> errorHandler.accept(error));
		return this;
	}
	private Optional<Consumer<Exception>> errorHandler = Optional.empty();
	
	@Override
	public ReplyHandler<Reply> onTimeout(Consumer<Void> timeoutHandler) {
		this.timeoutHandler = Optional.of(timeoutHandler);
		this.timeout.ifPresent(timeout -> timeoutHandler.accept(null));
		return this;
	}
	private Optional<Consumer<Void>> timeoutHandler = Optional.empty();
	
	
	public void doReply(Reply reply) {
		this.reply = Optional.of(reply);
		this.replyHandler.ifPresent(handler -> handler.accept(reply));
	}
	private Optional<Reply> reply = Optional.empty();
	
	public void doError(Exception ex) {
		this.error = Optional.of(ex);
		this.errorHandler.ifPresent(handler -> handler.accept(ex));
	}
	private Optional<Exception> error = Optional.empty();
	
	public void doTimeout() {
		this.timeout = Optional.of(true);
		this.timeoutHandler.ifPresent(handler -> handler.accept(null));
		this.errorHandler.ifPresent(handler -> handler.accept(new PiranyaException("Time out")));
	}
	private Optional<Boolean> timeout = Optional.empty();
	
}
