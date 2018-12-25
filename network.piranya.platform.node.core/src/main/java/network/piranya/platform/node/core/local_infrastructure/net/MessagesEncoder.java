package network.piranya.platform.node.core.local_infrastructure.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.Map;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import network.piranya.platform.api.accounting.AccountRef;
import network.piranya.platform.api.exceptions.IncompleteImplementationException;
import network.piranya.platform.api.exceptions.PiranyaException;
import network.piranya.platform.node.api.networking.nodes.Message;
import network.piranya.platform.node.core.local_infrastructure.security.MessageSecurityManager;
import network.piranya.platform.node.core.networks.index.client.SigninInfo;
import network.piranya.platform.node.core.networks.index.client.SigninRequest;
import network.piranya.platform.node.core.networks.index.client.messages.Ping;
import network.piranya.platform.node.core.networks.index.client.messages.Pong;
import network.piranya.platform.node.core.networks.index.cluster.messages.FollowupEntryMessage;
import network.piranya.platform.node.core.networks.index.cluster.messages.FollowupReply;
import network.piranya.platform.node.core.networks.index.cluster.messages.FollowupRequest;
import network.piranya.platform.node.core.networks.index.cluster.messages.Hi;
import network.piranya.platform.node.core.networks.index.cluster.messages.HiReply;
import network.piranya.platform.node.core.networks.index.cluster.messages.JoinClusterRequest;

public class MessagesEncoder {
	
	public network.piranya.infrastructure.pressing_udp.Message encode(Message message) {
		try {
			@SuppressWarnings("unchecked")
			Schema<Message> schema = (Schema<Message>)getSchema(message.getClass());
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			DataOutput dataOut = new DataOutputStream(outputStream);
			dataOut.writeShort(getMessageTypeId(message));
	        LinkedBuffer buffer = LinkedBuffer.allocate();
	        ProtobufIOUtil.writeTo(outputStream, message, schema, buffer);
			return new network.piranya.infrastructure.pressing_udp.Message(messageSecurityManager().generateSignedMessage(outputStream.toByteArray()));
		} catch (Throwable ex) {
			throw new PiranyaException(ex);
		}
	}
	
	public Message decode(network.piranya.infrastructure.pressing_udp.Message message) {
		try {
			AccountRef senderAccountRef = messageSecurityManager().getSenderAccountAndVerifySignature(message.buffer());
			
			ByteArrayInputStream inputStream = new ByteArrayInputStream(messageSecurityManager().messageData(message.buffer()));
			DataInput dataIn = new DataInputStream(inputStream);
			@SuppressWarnings("unchecked")
			Schema<Message> schema = (Schema<Message>)getSchema(getMessageType(dataIn.readShort()));
			
			Message m = schema.newMessage();
			ProtobufIOUtil.mergeFrom(inputStream, m, schema);
			m.setSourceAccountId(senderAccountRef);
			return m;
		} catch (Throwable ex) {
			throw new PiranyaException(ex);
		}
	}
	
	
	protected Short getMessageTypeId(Message message) {
		Short messageTypeId = idsToMessageTypes.get(message.getClass());
		if (messageTypeId == null) {
			throw new IncompleteImplementationException(String.format("Can not find message type id for '%s'", message.getClass().getName()));
		}
		return messageTypeId;
	}
	
	protected Class<?> getMessageType(short messageTypeId) {
		Class<?> messageType = messageTypesToIds.get(messageTypeId);
		if (messageType == null) {
			throw new IncompleteImplementationException(String.format("Can not find message type for type id '%s'", messageTypeId));
		}
		return messageType;
	}
	
	
	public MessagesEncoder(MessageSecurityManager messageSecurityManager) {
		this.messageSecurityManager = messageSecurityManager;
	}
	
	private final MessageSecurityManager messageSecurityManager;
	protected MessageSecurityManager messageSecurityManager() {
		return messageSecurityManager;
	}
	
	
	private final Map<Class<?>, Schema<?>> schemas = new HashMap<>();
	@SuppressWarnings("unchecked")
	protected <T> Schema<T> getSchema(Class<T> dataType) {
		Schema<T> schema = (Schema<T>)schemas.get(dataType);
		if (schema == null) {
			schema = RuntimeSchema.getSchema(dataType);
			schemas.put(dataType, schema);
		}
		return schema;
	}
	
	private static final Map<Short, Class<?>> messageTypesToIds = new HashMap<>();
	private static final Map<Class<?>, Short> idsToMessageTypes = new HashMap<>();
	static {
		messageTypesToIds.put((short)3000, SigninRequest.class);
		messageTypesToIds.put((short)3001, SigninInfo.class);
		messageTypesToIds.put((short)3002, Ping.class);
		messageTypesToIds.put((short)3003, Pong.class);
		messageTypesToIds.put((short)2000, Hi.class);
		messageTypesToIds.put((short)2001, HiReply.class);
		messageTypesToIds.put((short)2005, FollowupRequest.class);
		messageTypesToIds.put((short)2006, FollowupReply.class);
		messageTypesToIds.put((short)2007, FollowupEntryMessage.class);
		messageTypesToIds.put((short)2008, JoinClusterRequest.class);
		
		for (Map.Entry<Short, Class<?>> entry : messageTypesToIds.entrySet()) {
			idsToMessageTypes.put(entry.getValue(), entry.getKey());
		}
	}
	
}
