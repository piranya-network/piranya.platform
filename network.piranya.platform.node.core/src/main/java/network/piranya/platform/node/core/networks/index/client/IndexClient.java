package network.piranya.platform.node.core.networks.index.client;

import static network.piranya.platform.node.utilities.CollectionUtils.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import network.piranya.platform.api.accounting.NetworkCredentials;
import network.piranya.platform.api.exceptions.PiranyaException;
import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.api.lang.ReplyHandler;
import network.piranya.platform.node.api.booting.NetworkNodeConfig;
import network.piranya.platform.node.api.local_infrastructure.LocalServices;
import network.piranya.platform.node.api.local_infrastructure.LocalServicesProvider;
import network.piranya.platform.node.api.local_infrastructure.Log;
import network.piranya.platform.node.api.local_infrastructure.concurrency.Executor;
import network.piranya.platform.node.api.local_infrastructure.storage.specific.AccountsLocalDb;
import network.piranya.platform.node.api.networking.nodes.LocalNodeConfig;
import network.piranya.platform.node.api.networking.nodes.NodeChannelsProvider;
import network.piranya.platform.node.api.networking.nodes.NodeContacts;
import network.piranya.platform.node.api.networking.nodes.PublicClientChannelRef;
import network.piranya.platform.node.api.networking.nodes.Zone;
import network.piranya.platform.node.core.networks.index.client.messages.Ping;
import network.piranya.platform.node.core.networks.index.client.messages.Pong;
import network.piranya.platform.node.utilities.TimeService;
import network.piranya.platform.node.utilities.impl.ReplyHandlerImpl;

public class IndexClient {
	
	public ReplyHandler<SigninInfo> signin(NetworkCredentials credentials) {
		/**Plain English:
		 * If zone is not determined it, determine zone then do the sign in.
		 */
		setCredentials(credentials);
		ReplyHandlerImpl<SigninInfo> replyHandler = new ReplyHandlerImpl<>();
		boolean isZoneSet = nodeConfig().zone().isPresent();
		isZoneSet = true;
		if (!isZoneSet) {
			determineZone(zone -> {
				nodeConfig().updateZone(zone);
				isUpdateZoneOnNextPing = true;
				doSignin(credentials, replyHandler);
			}, error -> replyHandler.doError(new PiranyaException("Failed to determine zone: " + error.getMessage(), error)));
		} else {
			doSignin(credentials, replyHandler);
		}
		return replyHandler;
	}
	
	protected void doSignin(NetworkCredentials credentials, ReplyHandlerImpl<SigninInfo> replyHandler) {
		/**Plain English:
		 * Send Sign in request. On reply if refreshing zone is advised, determine zone, if different plan an update.
		 * Start the ping job and invoke reply handler.
		 */
		PublicClientChannelRef channel = channels().accessClientChannel(getSuitableIndexNode());
		try {
			System.out.println("channel");
			channel.talk(new SigninRequest(credentials, senderNodeContacts(credentials)), SigninInfo.class).onReply(reply -> {
				// TODO
				/*
				if (reply.isRefreshZone()) {
					determineZone(zone -> {
						if (!zone.equals(nodeConfig().zone().get())) {
							nodeConfig().updateZone(zone);
							isUpdateZoneOnNextPing = true;
						}
					}, error -> replyHandler.doError(new PiranyaException("Failed to determine zone: " + error.getMessage(), error)));
				}
				*/
				
				replyHandler.doReply(reply);
				
				executionManager().scheduleAtFixedRate(this::ping, 0L, 60_000L, TimeUnit.MILLISECONDS);
			})
			.onError(error -> replyHandler.doError(error));
		} catch (Exception ex) {
			replyHandler.doError(new PiranyaException(ex));
		}
	}
	
	
	protected void ping() {
		try (PublicClientChannelRef channel = channels().accessClientChannel(getSuitableIndexNode())) {
			Ping ping = new Ping(isUpdateZoneOnNextPing ? Ping.UPDATE_ZONE_INSTRUCTION : 0, getPingFlags(), accountingCycleTime());
			ping.setSenderContacts(senderNodeContacts(credentials()));
			channel.talk(ping, Pong.class)
					.onReply(pong -> {
						isUpdateZoneOnNextPing = false;
					})
					.onError(error -> { log().warning("Failed to send ping: " + error.getMessage(), error); });
		} catch (Exception ex) { }
	}
	
	private boolean isUpdateZoneOnNextPing = false;
	
	protected NodeContacts senderNodeContacts(NetworkCredentials credentials) {
		return new NodeContacts(config().nodeId(), "127.0.0.1", config().baseNetworkPort(), credentials.accountRef(), nodeConfig().zone().orElse(Zone.ANY));
	}
	
	protected int getPingFlags() {
		return 0;
	}
	
	private Optional<Long> accountingCycleTime = Optional.empty();
	protected Optional<Long> accountingCycleTime() {
		return accountingCycleTime;
	}
	protected void setAccountingCycleTime(Optional<Long> accountingCycleTime) {
		this.accountingCycleTime = accountingCycleTime;
	}
	
	
	// TODO
	public ReplyHandler<Object> getAccountTransactionsClusterInfo(long accountId) {
		return null;
	}
	
	public ReplyHandler<Object> getContractTransactionsClusterInfo(String descriptor) {
		return null;
	}
	
	public ReplyHandler<Object> queryContracts(String filter) {
		return null;
	}
	
	
	// TODO
	public void registerAccount() {
	}
	
	public void registerContractType() {
	}
	
	public void registerContract() {
	}
	
	
	public void dispose() {
		localServices().dispose();
	}
	
	
	protected void determineZone(Consumer<Zone> resultConsumer, Consumer<Exception> errorConsumer) {
		log().info("Determining local node's zone");
		Map<PublicClientChannelRef, NodeContacts> channelsOfNodes = new HashMap<>();
		foreach(indexNodesFromZones(2), nodeContacts -> channelsOfNodes.put(channels().accessClientChannel(nodeContacts), nodeContacts));
		
		ConcurrentMap<PublicClientChannelRef, Long> latencies1 = measureLatency(channelsOfNodes.keySet());
		ConcurrentMap<PublicClientChannelRef, Long> latencies2 = measureLatency(channelsOfNodes.keySet());
		
		if (latencies1.isEmpty() && latencies2.isEmpty()) {
			log().warning("Failed to connect to any of the index nodes to measure latency");
			errorConsumer.accept(new PiranyaException("Failed to connect to any of the index nodes to measure latency"));
		} else {
			Map.Entry<PublicClientChannelRef, Long> min1 = getMinEntry(latencies1);
			Map.Entry<PublicClientChannelRef, Long> min2 = getMinEntry(latencies2);
			boolean isMin1 = min1 == null || (min1 != null && min2 == null) || min1.getValue() <= min2.getValue();
			NodeContacts fastestNode = isMin1 ? channelsOfNodes.get(min1.getKey()) : channelsOfNodes.get(min2.getKey());
			log().info("Determined zone '%s' with latency roundtrip time %s milliseconds", fastestNode.zone(), isMin1 ? min1.getValue() : min2.getValue());
			resultConsumer.accept(fastestNode.zone());
		}
		
	}

	protected Map.Entry<PublicClientChannelRef, Long> getMinEntry(ConcurrentMap<PublicClientChannelRef, Long> latencies) {
		Map.Entry<PublicClientChannelRef, Long> min = null;
		for (Map.Entry<PublicClientChannelRef, Long> entry : latencies.entrySet()) {
			if (entry.getValue() != null && (min == null || entry.getValue() < min.getValue())) {
				min = entry;
			}
		}
		return min;
	}
	
	protected ConcurrentMap<PublicClientChannelRef, Long> measureLatency(Collection<PublicClientChannelRef> nodes) {
		ConcurrentMap<PublicClientChannelRef, Long> latencies = new ConcurrentHashMap<>();
		foreach(nodes, node -> node.talk(new Ping(Ping.CHECK_LATENCY_INSTRUCTION), Pong.class, 200).onReply(reply -> latencies.put(node, TimeService.now())));
		executionManager().waitUntil(() -> latencies.size() == nodes.size(), 200);
		return latencies;
	}
	
	
	protected List<NodeContacts> indexNodesFromZones(int countPerZone) {
		Map<Zone, List<NodeContacts>> nodesByZones = new HashMap<>();
		for (NodeContacts n : indexNodes()) {
			List<NodeContacts> list = nodesByZones.get(n.zone());
			if (list == null) {
				list = new ArrayList<>();
				nodesByZones.put(n.zone(), list);
			}
			if (list.size() < countPerZone) {
				list.add(n);
			}
		}
		
		List<NodeContacts> result = new ArrayList<>();
		for (List<NodeContacts> nodesOnZone : nodesByZones.values()) {
			result.addAll(nodesOnZone);
		}
		return result;
	}
	
	protected NodeContacts getSuitableIndexNode() {
		Zone zone = nodeConfig().zone().orElse(Zone.ANY);
		List<NodeContacts> nodes = filter(indexNodes(), n -> n.zone().isSuitable(zone));
		return nodes.get(random.nextInt(nodes.size()));
	}
	private final Random random = new Random(TimeService.now());
	
	protected List<NodeContacts> indexNodes() {
		return map(accountsLocalDb().indexNodes(), a -> a.activity().mainAccessPoint().nodeContacts());
	}
	
	
	public IndexClient(NetworkNodeConfig config, LocalNodeConfig nodeConfig, LocalServicesProvider localServicesProvider) {
		this.config = config;
		this.nodeConfig = nodeConfig;
		this.localServices = localServicesProvider.services(this);
		this.accountsLocalDb = localServices().localStorage().accountsLocalDb();
		this.channels = localServices().channelsProvider();
		this.executionManager = localServices().executor();
		this.log = localServices().log();
		
		/// having this as a constant hinders testing as index nodes are added after network node is created
		//this.indexNodes = map(accountsLocalDb().indexNodes(), a -> a.activity().mainAccessPoint().nodeContacts());
	}
	
	private final NetworkNodeConfig config;
	protected NetworkNodeConfig config() {
		return config;
	}
	
	private final LocalNodeConfig nodeConfig;
	protected LocalNodeConfig nodeConfig() {
		return nodeConfig;
	}
	
	private final LocalServices localServices;
	protected LocalServices localServices() {
		return localServices;
	}
	
	private final AccountsLocalDb accountsLocalDb;
	public AccountsLocalDb accountsLocalDb() {
		return accountsLocalDb;
	}
	
	private final NodeChannelsProvider channels;
	protected NodeChannelsProvider channels() {
		return channels;
	}
	
	private final Executor executionManager;
	protected Executor executionManager() {
		return executionManager;
	}
	
	private final Log log;
	protected Log log() {
		return log;
	}
	
	private NetworkCredentials credentials;
	protected NetworkCredentials credentials() {
		return credentials;
	}
	protected void setCredentials(NetworkCredentials credentials) {
		this.credentials = credentials;
	}
	
	//private final List<NodeContacts> indexNodes;
	
}
