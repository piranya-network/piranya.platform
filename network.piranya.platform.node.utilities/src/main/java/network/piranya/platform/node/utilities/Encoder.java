package network.piranya.platform.node.utilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import network.piranya.platform.api.exceptions.PiranyaException;

public class Encoder {
	
	public <T> byte[] encode(T data) {
		return encode(data, output -> {});
	}
	
	public <T> byte[] encode(T data, Consumer<DataOutput> prepender) {
		try {
			@SuppressWarnings("unchecked")
			Schema<T> schema = (Schema<T>)getSchema(data.getClass());
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			DataOutput dataOut = new DataOutputStream(outputStream);
			prepender.accept(dataOut);
			dataOut.writeShort(getDataTypeId(data.getClass()));
	        LinkedBuffer buffer = LinkedBuffer.allocate();
	        ProtobufIOUtil.writeTo(outputStream, data, schema, buffer);
			return outputStream.toByteArray();
		} catch (Throwable ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public <T> T decode(byte[] data) {
		return decode(data, 0, data.length);
	}
	
	public <T> T decode(byte[] data, int offset, int length) {
		try {
			ByteArrayInputStream inputStream = new ByteArrayInputStream(data, offset, length);
			DataInput dataIn = new DataInputStream(inputStream);
			Class<?> dataType = getDataType(dataIn.readShort());
			@SuppressWarnings("unchecked")
			Schema<T> schema = (Schema<T>)getSchema(dataType);
			T m = schema.newMessage();
			ProtobufIOUtil.mergeFrom(inputStream, m, schema);
			return m;
		} catch (Throwable ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public Class<?> getDataType(short dataTypeId) {
		Class<?> dataType = idsToDataTypes.get(dataTypeId);
		if (dataType == null) {
			throw new PiranyaException(String.format("Data Type for Type ID '%s' is not registered", dataTypeId));
		};
		return dataType;
	}
	
	public short getDataTypeId(Class<?> dataType) {
		Short typeId = dataTypesToIds.get(dataType);
		if (typeId == null) {
			throw new PiranyaException(String.format("Type ID for '%s' is not registered", dataType));
		}
		return typeId;
	}
	
	public void registerDataType(Class<?> dataType, short typeId) {
		dataTypesToIds.put(dataType, typeId);
		idsToDataTypes.put(typeId, dataType);
	}
	
	
	public Encoder() {
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
	
	private final Map<Short, Class<?>> idsToDataTypes = new HashMap<>();
	private final Map<Class<?>, Short> dataTypesToIds = new HashMap<>();
	
}
