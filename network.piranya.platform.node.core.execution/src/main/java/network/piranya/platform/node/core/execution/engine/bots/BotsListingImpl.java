package network.piranya.platform.node.core.execution.engine.bots;

import static network.piranya.platform.node.utilities.CollectionUtils.*;

import java.util.List;

import network.piranya.platform.api.exceptions.PiranyaException;
import network.piranya.platform.api.models.bots.BotRef;
import network.piranya.platform.api.models.bots.BotView;
import network.piranya.platform.api.models.bots.BotsListing;
import network.piranya.platform.api.models.metadata.BotTypeInfo;
import network.piranya.platform.node.api.execution.bots.BotsRegistry;

public class BotsListingImpl implements BotsListing {
	
	@Override
	public BotView bot(BotRef botRef) {
		checkIfDisposed();
		
		return registry().get(botRef).view();
	}
	
	@Override
	public List<BotView> list() {
		checkIfDisposed();
		
		return map(registry().botsList(), a -> a.view());
	}
	
	@Override
	public List<BotTypeInfo> botTypesByFeature(String featureId) {
		return map(registry().botTypesByFeature(featureId), bt -> registry().botMetadata(bt.getName()));
	}
	
	@Override
	public List<BotView> byType(String botTypeId) {
		checkIfDisposed();
		
		return filter(list(), b -> b.botTypeId().equals(botTypeId));
	}
	
	@Override
	public List<BotView> byFeature(String featureId) {
		checkIfDisposed();
		
		List<String> botTypes = map(registry().botTypesByFeature(featureId), bt -> bt.getName());
		return filter(list(), b -> botTypes.contains(b.botTypeId()));
	}
	
	@Override
	public BotView singleton(String botTypeId) {
		checkIfDisposed();
		
		List<BotView> bots = byType(botTypeId);
		if (bots.size() == 1) {
			return bots.get(0);
		} else {
			throw new PiranyaException(String.format("Singleton Bot '%s' is not registered", botTypeId));
		}
	}
	
	@Override
	public BotTypeInfo botMetadata(String botTypeId) {
		checkIfDisposed();
		
		return registry().botMetadata(botTypeId);
	}
	
	@Override
	public List<BotTypeInfo> botTypes() {
		checkIfDisposed();
		
		return registry().botTypeInfoList();
	}
	
	
	public BotsListingImpl(BotsRegistry registry) {
		this.registry = registry;
	}
	
	private final BotsRegistry registry;
	protected BotsRegistry registry() {
		return registry;
	}
	
	public void checkIfDisposed() {
		if (disposed) {
			throw new PiranyaException("Context is disposed");
		}
	}
	
	public void dispose() {
		this.disposed = true;
	}
	private boolean disposed = false;
	
}
