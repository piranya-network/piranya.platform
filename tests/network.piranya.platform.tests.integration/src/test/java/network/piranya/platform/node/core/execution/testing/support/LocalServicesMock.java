package network.piranya.platform.node.core.execution.testing.support;

import java.util.Map;

import network.piranya.platform.api.accounting.NetworkCredentials;
import network.piranya.platform.node.api.booting.NetworkNodeConfig;
import network.piranya.platform.node.api.local_infrastructure.LocalServices;
import network.piranya.platform.node.api.local_infrastructure.LocalServicesProvider;
import network.piranya.platform.node.api.networking.nodes.NodeContacts;
import network.piranya.platform.node.core.local_infrastructure.LocalServicesImpl;
import network.piranya.platform.node.core.local_infrastructure.LocalServicesProviderImpl;
import network.piranya.platform.node.core.local_infrastructure.net.NodeChannelsProviderImpl;
import network.piranya.platform.node.core.local_infrastructure.net.PublicClientChannel;
import network.piranya.platform.node.utilities.ReflectionUtils;

public class LocalServicesMock {
	
	@SuppressWarnings("unchecked")
	public Map<NodeContacts, PublicClientChannel> clientChannels() {
		return (Map<NodeContacts, PublicClientChannel>)ReflectionUtils.getFieldValue(
				(NodeChannelsProviderImpl)services.channelsProvider(), NodeChannelsProviderImpl.class, "publicClientChannels");
	}
	
	public LocalServicesProvider provider() {
		return provider;
	}
	
	
	public LocalServicesMock(NetworkNodeConfig config, NetworkCredentials credentials) {
		this.provider = new LocalServicesProviderImpl(config, credentials) {
			@Override public LocalServices services(Object owner) {
				if (services == null) {
					services = (LocalServicesImpl)super.services(owner);
				}
				return services;
			}
		};
		provider.services(this);
	}
	
	private LocalServicesProviderImpl provider;
	private LocalServicesImpl services;
	
}
