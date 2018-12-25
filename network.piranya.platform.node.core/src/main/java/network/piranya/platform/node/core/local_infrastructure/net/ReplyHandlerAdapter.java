package network.piranya.platform.node.core.local_infrastructure.net;

import java.util.function.Consumer;

import network.piranya.infrastructure.pressing_udp.utilities.SendingResultHandler;
import network.piranya.infrastructure.pressing_udp.utilities.TimeService;
import network.piranya.platform.api.exceptions.PiranyaException;
import network.piranya.platform.node.utilities.impl.ReplyHandlerImpl;

public class ReplyHandlerAdapter<Reply> extends ReplyHandlerImpl<Reply> {
	
	@SuppressWarnings("unchecked")
	public ReplyHandlerAdapter(SendingResultHandler sendingResultHandler, ChannelsContext context, Consumer<Long> latencyConsumer) {
		final long requestTime = TimeService.now();
		sendingResultHandler
				.onReply(reply -> {
					latencyConsumer.accept(TimeService.now() - requestTime);
					doReply((Reply)context.messagesEncoder().decode(reply));
				})
				.onTimeout(timeout -> doTimeout())
				.onError(error -> doError(new PiranyaException(error.getMessage(), error)));
	}
	
}
