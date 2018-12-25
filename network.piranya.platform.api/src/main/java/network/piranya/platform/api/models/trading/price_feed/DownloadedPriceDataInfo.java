package network.piranya.platform.api.models.trading.price_feed;

import java.util.Set;

public class DownloadedPriceDataInfo {
	
	private String key;
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	
	private String queueId;
	public String getQueueId() {
		return queueId;
	}
	public void setQueueId(String queueId) {
		this.queueId = queueId;
	}
	
	private String dataType;
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	
	private Set<String> symbols;
	public Set<String> getSymbols() {
		return symbols;
	}
	public void setSymbols(Set<String> symbols) {
		this.symbols = symbols;
	}
	
	private long startTime;
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	
	private long endTime;
	public long getEndTime() {
		return endTime;
	}
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	
	private int recordsCount;
	public int getRecordsCount() {
		return recordsCount;
	}
	public void setRecordsCount(int recordsCount) {
		this.recordsCount = recordsCount;
	}
	
	public DownloadedPriceDataInfo() {
	}
	
	public DownloadedPriceDataInfo(String key, String queueId, String dataType, Set<String> symbols, long startTime, long endTime, int recordsCount) {
		this.key = key;
		this.queueId = queueId;
		this.dataType = dataType;
		this.symbols = symbols;
		this.startTime = startTime;
		this.endTime = endTime;
		this.recordsCount = recordsCount;
	}
	
	public static final String DB_LIST_PREFIX = "downloaded-price-data";
	
}
