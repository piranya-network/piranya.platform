package network.piranya.platform.node.core.execution.engine.sandbox;

import java.util.function.Consumer;
import java.util.function.Function;

import network.piranya.platform.api.exceptions.PiranyaException;
import network.piranya.platform.api.extension_models.execution.sandbox.SandboxConfiguration;
import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.api.models.analytics.AnalyticalViewData;
import network.piranya.platform.api.models.bots.BotsListing;
import network.piranya.platform.api.models.execution.sandbox.Sandbox;
import network.piranya.platform.node.core.execution.engine.bots.BotsListingImpl;

public class SandboxImpl implements Sandbox {
	
	@Override
	public void runAsync() {
		sandboxEngine.ifPresent(engine -> engine.abort());
		this.sandboxEngine = Optional.of(sandboxEngineFactory().apply(configuration()));
		sandboxEngine.get().onFinishListener(finishListener);
		
		sandboxEngine.get().run();
	}
	
	@Override
	public void abort() {
		sandboxEngine().ifPresent(se -> se.abort());
	}
	
	@Override
	public BotsListing bots() {
		return new BotsListingImpl(sandboxEngine().get().getBotsRegistry());
	}
	
	@Override
	public <ViewDataType> AnalyticalViewData<ViewDataType> getAnalyticalView(String viewId) {
		return sandboxEngine.orElseThrow(() -> new PiranyaException("Sandbox is not running")).analyticsEngine().getView(viewId);
	}
	
	@Override
	public void onFinish(Runnable listener) {
		this.finishListener = listener;
	}
	private Runnable finishListener;
	
	@Override
	public void onError(Consumer<Exception> listener) {
	}
	
	
	public SandboxImpl(SandboxConfiguration configuration, Function<SandboxConfiguration, SandboxEngine> sandboxEngineFactory) {
		this.configuration = configuration;
		this.sandboxEngineFactory = sandboxEngineFactory;
	}
	
	private final SandboxConfiguration configuration;
	protected SandboxConfiguration configuration() { return configuration; }
	
	private final Function<SandboxConfiguration, SandboxEngine> sandboxEngineFactory;
	protected Function<SandboxConfiguration, SandboxEngine> sandboxEngineFactory() { return sandboxEngineFactory; }
	
	private Optional<SandboxEngine> sandboxEngine = Optional.empty();
	protected Optional<SandboxEngine> sandboxEngine() { return sandboxEngine; }
	
}
