package network.piranya.platform.node.core.local_infrastructure.net.utilities;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import network.piranya.platform.api.lang.ReplyHandler;
import network.piranya.platform.node.api.networking.nodes.GroupMemberChannelRef;
import network.piranya.platform.node.api.networking.nodes.Message;
import network.piranya.platform.node.utilities.RefImpl;
import network.piranya.platform.node.utilities.impl.ReplyHandlerImpl;

public final class ChannelUtils {
	
	public static <Reply> void waitAndPickReply(List<ReplyHandler<Reply>> replyHandlers, int waitingPeriod, Comparator<Reply> repliesComparator,
			Consumer<Reply> everyReplyHandler, Consumer<Reply> bestReplyHandler, Consumer<Void> noRepliesHandler, Consumer<Void> afterHandler) {
		
	}
	
	public static <Request extends Message, Reply extends Message> ReplyHandler<Reply> sendToFirstPossible(
			Request request, Class<Reply> replyType, List<GroupMemberChannelRef> channels, int waitingPeriod) {
		final ReplyHandlerImpl<Reply> replyHandler = new ReplyHandlerImpl<>();
		final Iterator<GroupMemberChannelRef> iterator = channels.iterator();
		final RefImpl<Runnable> tryNextRef = new RefImpl<>();
		tryNextRef.set(() -> {
			if (iterator.hasNext()) {
				GroupMemberChannelRef channel = iterator.next();
				channel.talk(request, replyType, waitingPeriod).onReply(reply -> replyHandler.doReply(reply)).onTimeout(timeout -> tryNextRef.get().run());
			} else {
				replyHandler.doTimeout();
			}
		});
		tryNextRef.get().run();
		return replyHandler;
	}
	
	
	private ChannelUtils() { }
	
}
