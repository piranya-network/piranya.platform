package network.piranya.platform.node.core.execution.testing.support;

import java.io.File;
import java.util.function.Consumer;
import java.util.function.Supplier;

import network.piranya.platform.api.extension_models.ParametersBuilder;
import network.piranya.platform.api.lang.Result;
import network.piranya.platform.api.models.bots.BotRef;
import network.piranya.platform.api.models.bots.BotView;
import network.piranya.platform.node.core.execution.testing.utils.FileUtils;
import network.piranya.platform.node.utilities.RefImpl;

public class AbstractExecutionTest {
	
	protected File createTempDir() {
		return FileUtils.createTempDir(true);
	}
	
	protected Consumer<Result<BotView>> newBotRefHandler(RefImpl<BotRef> actorRefReference) {
		return result -> {
			if (result.isSuccessful()) {
				actorRefReference.set(result.result().get().ref());
			} else {
				throw new RuntimeException(result.error().get());
			}
		};
	}
	
	protected Consumer<Result<BotView>> newBotViewHandler(RefImpl<BotView> actorViewReference) {
		return result -> {
			if (result.isSuccessful()) {
				actorViewReference.set(result.result().get());
			} else {
				throw new RuntimeException(result.error().get());
			}
		};
	}
	
	protected boolean waitUntil(long time, Supplier<Boolean> condition) {
		long cnt = 0;
		while (cnt++ <= time) {
			try {
				if (condition.get() == true) return true;
				Thread.sleep(1);
			} catch (Exception e) {}
		}
		return false;
	}
	
	protected ParametersBuilder params() {
		return new ParametersBuilder();
	}
	
}
