package network.piranya.platform.api.models.infrastructure.net;

import network.piranya.platform.api.lang.ReplyHandler;

public interface RestService {
	
	//ReplyHandler<Object> get(String url);
	
	//ReplyHandler<Object> get(String url, HttpRequestParams params);
	
	ReplyHandler<JsonStreamReader> processLargeJson(String url);
	
	ReplyHandler<JsonStreamReader> getLargeJson(String url, HttpRequestParams params);
	
}
