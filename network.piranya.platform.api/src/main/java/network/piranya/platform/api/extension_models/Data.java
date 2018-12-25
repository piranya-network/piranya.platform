package network.piranya.platform.api.extension_models;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import network.piranya.platform.api.lang.Optional;

import network.piranya.platform.api.exceptions.InvalidParameterException;
import network.piranya.platform.api.exceptions.PiranyaException;
import network.piranya.platform.api.models.trading.Instrument;
import network.piranya.platform.api.utilities.Utilities;

public class Data {
	
	public Optional<String> optionalString(String key) {
		try { return Optional.of(string(key)); }
		catch (Throwable ex) { return Optional.empty(); }
	}
	
	public String string(String key) {
		return (String)get(key);
	}
	
	public String string(String key, String defaultValue) {
		return optionalString(key).orElse(defaultValue);
	}
	
	public boolean bool(String key) {
		Object value = get(key);
		return value instanceof String ? Boolean.valueOf((String)value) : (Boolean)value;
	}
	
	public boolean bool(String key, boolean defaultValue) {
		Object value = dataMap().get(key);
		return value != null ? (value instanceof String ? Boolean.valueOf((String)value) : (Boolean)value) : defaultValue;
	}
	
	public BigDecimal decimal(String key) {
		Object value = get(key);
		return value instanceof String ? new BigDecimal((String)value) : (BigDecimal)value;
	}
	
	public int integer(String key) {
		Object value = get(key);
		return value instanceof String ? Integer.valueOf((String)value) : (Integer)value;
	}
	
	public int integer(String key, int defaultValue) {
		Object value = dataMap().get(key);
		return value != null ? (value instanceof String ? Integer.valueOf((String)value) : (Integer)value) : defaultValue;
	}
	
	public long longv(String key) {
		Object value = get(key);
		return value instanceof String ? Long.valueOf((String)value) : (Long)value;
	}
	
	public Instrument instrument(String key) {
		return toInstrument(string(key), key);
	}
	
	public List<String> list(String key) {
		return stringList(key);
	}
	
	@SuppressWarnings("unchecked")
	public List<String> stringList(String key) {
		Object value = dataMap().get(key);
		if (value != null && !(value instanceof List)) {
			System.out.println(value);
			System.out.println(value.getClass());
			throw new InvalidParameterException("Attmpting to read parameter '%s' as list while it's not", key);
		}
		return value != null ? (List<String>)value : Collections.emptyList();
	}
	
	public List<Instrument> instrumentList(String key) {
		return utils.col.map(stringList(key), s -> new Instrument(s));
	}
	
	public List<Integer> integerList(String key) {
		return utils.col.map(stringList(key), s -> Integer.valueOf(s));
	}
	
	public List<Long> longList(String key) {
		return utils.col.map(stringList(key), s -> Long.valueOf(s));
	}
	
	public List<BigDecimal> decimalList(String key) {
		return utils.col.map(stringList(key), s -> new BigDecimal(s));
	}
	
	public List<Boolean> booleanList(String key) {
		return utils.col.map(stringList(key), s -> Boolean.valueOf(s));
	}
	
	public long dateTime(String key) {
		return getTime(key, DATE_TIME_FORMAT);
	}
	
	public long date(String key) {
		return getTime(key, DATE_FORMAT);
	}
	
	public long time(String key) {
		return getTime(key, TIME_FORMAT);
	}
	
	private long getTime(String key, SimpleDateFormat format) {
		Object value = get(key);
		try {
			return value instanceof String ? format.parse((String)value).getTime() : (Long)value;
		} catch (ParseException ex) {
			throw new InvalidParameterException(InvalidParameterException.INVALID_VALUE_MESSAGE, key);
		}
	}
	
	public boolean contains(String key) {
		return dataMap().containsKey(key);
	}
	
	
	protected Object get(String key) {
		Object value = dataMap().get(key);
		if (value == null) {
			throw new InvalidParameterException(InvalidParameterException.REQUIRED_MESSAGE, key);
		}
		return value;
	}
	
	
	public Data(Map<String, Object> data) {
		this.data = data;
	}
	
	public Data(Data data) {
		this(data.dataMap());
	}
	
	public Data() {
		this(new HashMap<>());
	}
	
	private final Map<String, Object> data;
	public Map<String, Object> dataMap() {
		return data;
	}
	
	
	private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
	
	private static final Utilities utils = new Utilities();
	
	private static Instrument toInstrument(String symbol, String key) {
		try {
			return new Instrument(symbol);
		} catch (IllegalArgumentException ex) {
			throw new InvalidParameterException(ex.getMessage(), key, false);
		} catch (PiranyaException ex) {
			throw new InvalidParameterException(ex.getMessage(), key, false);
		}
	}

}
