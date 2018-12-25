package network.piranya.platform.node.core.execution.engine.infrastructure.net;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import network.piranya.platform.api.lang.ReplyHandler;
import network.piranya.platform.api.models.infrastructure.net.HttpRequestParams;
import network.piranya.platform.api.models.infrastructure.net.JsonStreamReader;
import network.piranya.platform.api.models.infrastructure.net.RestService;
import network.piranya.platform.node.api.local_infrastructure.concurrency.Executor;
import network.piranya.platform.node.core.execution.infrastructure.serialization.json.JsonStreamReaderImpl;
import network.piranya.platform.node.utilities.FileUtils;
import network.piranya.platform.node.utilities.impl.ReplyHandlerImpl;

public class RestServiceImpl implements RestService {
	
	@Override
	public ReplyHandler<JsonStreamReader> processLargeJson(String url) {
		return getLargeJson(url, new HttpRequestParams());
	}
	
	@Override
	public ReplyHandler<JsonStreamReader> getLargeJson(String url, HttpRequestParams params) {
		ReplyHandlerImpl<JsonStreamReader> replyHandler = new ReplyHandlerImpl<>();
		executor().execute(() -> {
			File tempDataFile = null;
			try {
				URL website = new URL(url);
				try (InputStream httpIn = website.openStream()) {
				    tempDataFile = FileUtils.createTempFile(true);
					Files.copy(httpIn, tempDataFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
					
					try (InputStream fileIn  = new FileInputStream(tempDataFile)) {
						replyHandler.doReply(new JsonStreamReaderImpl(fileIn));
					}
					tempDataFile.delete();
				}
			} catch (Exception ex) {
				if (tempDataFile != null && tempDataFile.exists()) {
					try { tempDataFile.delete(); } catch (Throwable ex2) { }
				}
				
				replyHandler.doError(ex);
			}
		});
		return replyHandler;
	}
	
	public RestServiceImpl(Executor executor) {
		this.executor = executor;
	}
	
	private final Executor executor;
	protected Executor executor() { return executor; }
	
}
