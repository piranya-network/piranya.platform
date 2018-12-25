package network.piranya.platform.node.core.networks.index.cluster;

import static network.piranya.platform.node.utilities.CollectionUtils.*;
import static network.piranya.platform.node.core.local_infrastructure.net.utilities.ChannelUtils.*;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import network.piranya.platform.api.accounting.AccountRef;
import network.piranya.platform.api.accounting.NetworkCredentials;
import network.piranya.platform.api.exceptions.PiranyaException;
import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.node.accounts.Account;
import network.piranya.platform.node.api.local_infrastructure.LocalServices;
import network.piranya.platform.node.api.local_infrastructure.LocalServicesProvider;
import network.piranya.platform.node.api.local_infrastructure.storage.specific.AccountsLocalDb;
import network.piranya.platform.node.api.networking.nodes.ErrorMessage;
import network.piranya.platform.node.api.networking.nodes.GroupChannelRef;
import network.piranya.platform.node.api.networking.nodes.GroupMemberChannelRef;
import network.piranya.platform.node.api.networking.nodes.HeartbeatMessage;
import network.piranya.platform.node.api.networking.nodes.NodeChannelsProvider;
import network.piranya.platform.node.api.networking.nodes.NodeChannelsProvider.ChannelGroupType;
import network.piranya.platform.node.api.networking.nodes.NodeContacts;
import network.piranya.platform.node.api.networking.nodes.PublicServiceChannelRef;
import network.piranya.platform.node.api.networking.nodes.ReplySender;
import network.piranya.platform.node.core.networks.index.client.SigninInfo;
import network.piranya.platform.node.core.networks.index.client.SigninRequest;
import network.piranya.platform.node.core.networks.index.client.messages.Ping;
import network.piranya.platform.node.core.networks.index.client.messages.Pong;
import network.piranya.platform.node.core.networks.index.client.messages.RecordFillMessage;
import network.piranya.platform.node.core.networks.index.cluster.messages.FollowupEntryMessage;
import network.piranya.platform.node.core.networks.index.cluster.messages.FollowupReply;
import network.piranya.platform.node.core.networks.index.cluster.messages.FollowupRequest;
import network.piranya.platform.node.core.networks.index.cluster.messages.Hi;
import network.piranya.platform.node.core.networks.index.cluster.messages.HiReply;
import network.piranya.platform.node.core.networks.index.cluster.messages.IndexClusterDetails;
import network.piranya.platform.node.core.networks.index.cluster.messages.IndexClusterMessage;
import network.piranya.platform.node.core.networks.index.cluster.messages.JoinClusterRequest;
import network.piranya.platform.node.utilities.Batch;
import network.piranya.platform.node.utilities.TimeService;

// TODO if disconnects for not too long, heartbeat receiving will be fine, but some events could be missed. How to detect? Currently, assuming Aeron layer will deliver reliably.
public class IndexCluster {
	
	public void boot() {
		/**Plain English:
		 * Retrieve certified Index accounts from local accounts db and create Index nodes from them.
		 * Send hi to each, on receiving declare that node as alive. Process index cluster details from each to look for missing nodes.
		 * Send followup request to fastest replier.
		 * After followup process is complete, join cluster.
		 */
		foreach(map(filter(accountsLocalDb().indexNodes(), account -> !account.accountId().equals(credentials().accountRef())), account -> new IndexNode(account, channels())),
				node -> indexNodes().put(node.account().accountId(), node));
		
		foreach(indexNodes().values(), node -> node.hi().onReply(reply -> onHiReply(node, reply)).onError(ex -> onHiErrorOrTimeout(node)));
		setInitiallyContactedNodes(indexNodes().size());
	}
	
	public void shutdown() {
		localServices().dispose();
	}
	
	
	protected void onHiReply(IndexNode node, HiReply reply) {
		/**Plain English:
		 * See the Index nodes that the replier is connected to. If they are not on our list, add them to extra nodes. When followup is complete, we will connect to them.
		 * If followup is not started and the replies amount is equal or larger than the amount of valid Index nodes, start the followup process.
		 */
		List<NodeContacts> extraNodes = subtract(reply.indexClusterDetails().nodes(), map(indexNodes().values(), n -> n.account().activity().mainAccessPoint().nodeContacts()));
		foreach(extraNodes, n -> extraNodes().putIfAbsent(n.accountId(), n));
		
		if (!isFollowupStarted && hiRepliesCounter.incrementAndGet() >= indexNodes().size()) {
			followup();
		}
	}
	
	protected void onHiErrorOrTimeout(IndexNode node) {
		/**Plain English:
		 * If a node responded with error to our Hi or timedout, ignore it (remove from Index nodes list).
		 */
		indexNodes().remove(node.account().accountId());
		
		if (!isFollowupStarted && hiRepliesCounter.get() >= indexNodes().size()) {
			followup();
		}
	}
	private final AtomicInteger hiRepliesCounter = new AtomicInteger(0);
	
	protected void followup() {
		/**Plain English:
		 * Send node with lowest latency a followup request.
		 */
		this.isFollowupStarted = true;
		if (indexNodes().isEmpty()) {
			throw new PiranyaException("No Index nodes are available"); // TODO fail completely or just reboot
		}
		
		List<GroupMemberChannelRef> indexNodesChannels = map(sortBy(indexNodes().values(), node -> node.latency().orElse(Integer.MAX_VALUE)), node -> node.channel());
		sendToFirstPossible(prepareFollowupRequest(), FollowupReply.class, indexNodesChannels, 300)
				.onReply(reply -> followupSourceAccountId = reply.sourceAccountId())
				.onTimeout(timeout -> {}); // TODO fail completely or just reboot
	}
	private boolean isFollowupStarted = false;
	private AccountRef followupSourceAccountId;
	
	protected FollowupRequest prepareFollowupRequest() {
		return new FollowupRequest(accountsLocalDb().lastUpdateTime());
	}
	
	protected void onFollowupEntry(FollowupEntryMessage message) {
		if (this.isFollowupStarted && message.sourceAccountId() == this.followupSourceAccountId) {
			message.account().ifPresent(account -> followupAccountsBatch.feed(account));
			//message.accountingClusterSpec().ifPresent(clusterSpec -> accountingAssignmentsManager().followup(clusterSpec));
			
			if (message.isFollowupComplete()) {
				followupAccountsBatch.finish();
				doJoinCluster();
			}
		}
	}
	private final Batch<Account> followupAccountsBatch = new Batch<>(1000, accounts -> accountsLocalDb().putAccounts(accounts));
	
	protected void doJoinCluster() {
		/**Plain English:
		 * Send join cluster request to each Index node on our list.
		 * Extra nodes are the nodes that are not registered in our database as Index nodes but we got their info from other Index nodes. We connect to them and join them too.
		 * Also, start sending heartbeats.
		 */
		foreach(indexNodes().values(), node -> node.joinCluster());
		
		List<IndexNode> extraNodes = flatMap(extraNodes().values(), n -> accountsLocalDb().findAccount(n.accountId()).map(account -> new IndexNode(account, channels())));
		foreach(extraNodes, n -> {
			indexNodes().put(n.account().accountId(), n);
			n.hi().onReply(reply -> n.joinCluster()).onError(ex -> extraNodes().remove(n.account().accountId()));
		});
		
		localServices().executor().scheduleAtFixedRate(() -> groupChannel().publish(new HeartbeatMessage()), HEARTBEAT_PERIOD, HEARTBEAT_PERIOD, TimeUnit.MILLISECONDS);
		
		//accountingAssignmentsManager().init();
	}
	
	
	protected void onHiRequest(Hi hi) {
		/**Plain English:
		 * If node is contained on our local accounts database, add it to pending, send it Hi Reply. And wait for it to request joining cluster, then it's officially another Index node.
		 */
		accountsLocalDb().findAccount(hi.sourceAccountId()).ifPresent(account -> {
			IndexNode indexNode = new IndexNode(account, channels());
			pendingNodes().put(indexNode.account().accountId(), indexNode);
			indexNode.channel().send(new HiReply(prepareIndexClusterDetails()));
		});
	}
	
	protected IndexClusterDetails prepareIndexClusterDetails() {
		return new IndexClusterDetails(map(indexNodes().values(), node -> head(node.account().accessNodes())));
	}
	
	protected void onFollowupRequest(FollowupRequest request) {
		/**Plain English:
		 * Get all updated objects after the specified time, and send them to that node.
		 * This operation is chunked.
		 */
		IndexNode node = pendingNodes().get(request.sourceAccountId());
		if (node != null) {
			node.channel().send(new FollowupReply());
			node.channel().sendChunk().iterate(accountsLocalDb().accountsIterator(account -> account.lastUpdateTime() >= request.lastAccountsUpdateTime()),
					account -> new FollowupEntryMessage(account), () -> Optional.of(new FollowupEntryMessage(true)));
		}
		
		// TODO share next cluster if that node has none
	}
	
	protected void onJoinRequest(JoinClusterRequest request) {
		/**Plain English:
		 * If node is in Pending status (basically it sent us Hi and we accepted it), add it to the Index nodes list and reply with confirmation.
		 */
		IndexNode node = pendingNodes().get(request.sourceAccountId());
		if (node != null) {
			indexNodes().putIfAbsent(request.sourceAccountId(), node);
			pendingNodes().remove(request.sourceAccountId());
		}
	}
	
	
	protected void onHeartbeatRequest(HeartbeatMessage message) {
		/**Plain English:
		 * If the time since last heartbeat is beyond a certain timeout, then we are probably off the grid for some time provided that we received nothing from any node.
		 * If such is the case, reboot the cluster object.
		 * If all is fine, just refresh heartbeat time values.
		 */
		IndexNode indexNode = indexNodes().get(message.sourceAccountId());
		if (indexNode != null) {
			if (lastHearbeatTime() != 0L && TimeService.now() - lastHearbeatTime() > INACTIVITY_TIMEOUT) {
				rebootOperation().run();
			}
			
			indexNode.updateHeartbeat();
			updateLastHeartbeatTime();
		}
	}
	
	
	protected void onAccountTransaction() {
		/**Plain English:
		 * Simplified option for now. For now, queue and batch every 500 milliseconds, send append to each node with prev account version. (only transactions are account security)
		 * On majority replying, send reply to the AC.
		 * As for activity, update once every minute for trading and once every 5 seconds for address change.
		 */
		// apply (if account security, check last version)
		// queue
		//map(indexNodes().keySet(), node -> node.hi());
	}
	
	protected void onSignin(SigninRequest request, ReplySender<SigninInfo> replySender) {
		System.err.println("----------- onSignin");
		Optional<Account> account = accountsLocalDb().findAccount(request.sourceAccountId());
		if (request.sourceAccountId().equals(request.credentials().accountRef())) {
			Optional<NodeContacts> currentNodeContacts = find(account.get().accessNodes(), n -> n.nodeId().equals(request.senderContacts().nodeId()));
			boolean refreshZone = currentNodeContacts.isPresent() && !currentNodeContacts.get().equals(request.senderContacts());
			
			replySender.sendReply(new SigninInfo(account.get(), /*accountingAssignmentsManager().getClusterSpecForAccount(account.get()), */refreshZone/*,
					accountingAssignmentsManager().liveCycleTime(), AccountingAssignmentsManager.CYCLE_PERIOD*/));
			System.out.println("sent1");
		} else {
			replySender.sendError(new ErrorMessage("", "", String.format("",
					request.sourceAccountId(), request.credentials().accountRef())));
		}
	}
	
	protected void onPing(Ping ping, ReplySender<Pong> replySender) {
		/**Plain English:
		 * If received address is different from currently stored. Advise to refresh latency test, reply with a list of index nodes to request latency from.
		 * Wait for latency result from 
		 */
		if (ping.hasInstruction(Ping.CHECK_LATENCY_INSTRUCTION)) {
			replySender.sendReply(new Pong());
			return;
		}
		
		Optional<Account> account = accountsLocalDb().findAccount(ping.sourceAccountId());
		if (account.isPresent()) {
			accountsLocalDb().updateAddressIfNeeded(ping.sourceAccountId(), ping.senderContacts());
			// update node activity stats (for example to know which node is the most active one)
			replySender.sendReply(new Pong());
		} else {
			// would it ever be the case?
		}
	}
	
	protected void onFill(RecordFillMessage message) {
		accountsLocalDb().recordFill(message.sourceAccountId(), message.fillVolume());
	}
	
	protected void onAccountChange() {
		// queue, to publish changes to watchdogs by checking accountingClusterSpec(accountId) for accounts watchdogs
	}
	
	
	protected List<IndexNode> liveNodes() {
		long now = TimeService.now();
		return filter(indexNodes().values(), n -> now - n.lastHeartbeatTime() < 10_000L);
	}
	
	
	public IndexCluster(NetworkCredentials credentials, LocalServicesProvider localServicesProvider, Runnable rebootOperation) {
		this.credentials = credentials;
		this.rebootOperation = rebootOperation;
		this.localServices = localServicesProvider.services(this);
		this.accountsLocalDb = localServices.localStorage().accountsLocalDb();
		this.channels = localServices().channelsProvider();
		this.groupChannel = channels().useGroupChannel(ChannelGroupType.INDEX_CLUSTER, 0, IndexClusterMessage.class, message -> true);
		this.publicChannel = channels().usePublicServiceChannel();
		//this.accountingAssignmentsManager = new AccountingAssignmentsManager(credentials.accountId(), localServices(), groupChannel(), accountsLocalDb(), () -> liveNodes());
		
		groupChannel().subscribe(Hi.class, this::onHiRequest);
		groupChannel().subscribe(JoinClusterRequest.class, this::onJoinRequest);
		groupChannel().subscribe(FollowupRequest.class, this::onFollowupRequest);
		groupChannel().subscribe(FollowupEntryMessage.class, this::onFollowupEntry);
		groupChannel().subscribe(HeartbeatMessage.class, this::onHeartbeatRequest);
		
		publicChannel().subscribe(SigninRequest.class, this::onSignin);
		publicChannel().subscribe(Ping.class, this::onPing);
		publicChannel().subscribe(RecordFillMessage.class, this::onFill);
		//publicChannel().subscribe(AccountAccountingClusterSpecRequest.class, this::onAccountAccountingClusterSpecRequest);
		// account creation, security update
		// query modules
		// query contracts
	}
	
	private final PublicServiceChannelRef publicChannel;
	protected PublicServiceChannelRef publicChannel() {
		return publicChannel;
	}
	
	private final NetworkCredentials credentials;
	public NetworkCredentials credentials() {
		return credentials;
	}
	
	private final AccountsLocalDb accountsLocalDb;
	public AccountsLocalDb accountsLocalDb() {
		return accountsLocalDb;
	}
	
	private final LocalServices localServices;
	protected LocalServices localServices() {
		return localServices;
	}
	
	private final NodeChannelsProvider channels;
	protected NodeChannelsProvider channels() {
		return channels;
	}
	
	private final ConcurrentMap<AccountRef, IndexNode> indexNodes = new ConcurrentHashMap<>();
	protected ConcurrentMap<AccountRef, IndexNode> indexNodes() {
		return indexNodes;
	}
	
	private final ConcurrentMap<AccountRef, IndexNode> pendingNodes = new ConcurrentHashMap<>();
	protected ConcurrentMap<AccountRef, IndexNode> pendingNodes() {
		return pendingNodes;
	}
	
	private final ConcurrentMap<AccountRef, NodeContacts> extraNodes = new ConcurrentHashMap<>();
	protected ConcurrentMap<AccountRef, NodeContacts> extraNodes() {
		return extraNodes;
	}
	
	private int initiallyContactedNodes;
	protected int initiallyContactedNodes() {
		return initiallyContactedNodes;
	}
	protected void setInitiallyContactedNodes(int initiallyContactedNodes) {
		this.initiallyContactedNodes = initiallyContactedNodes;
	}
	
	private long lastHearbeatTime = 0L;
	protected void updateLastHeartbeatTime() {
		this.lastHearbeatTime = TimeService.now();
	}
	protected long lastHearbeatTime() {
		return this.lastHearbeatTime;
	}
	
	private final Runnable rebootOperation;
	protected Runnable rebootOperation() {
		return rebootOperation;
	}
	
	private final GroupChannelRef groupChannel;
	protected GroupChannelRef groupChannel() {
		return groupChannel;
	}
	
	/*
	private final AccountingAssignmentsManager accountingAssignmentsManager;
	protected AccountingAssignmentsManager accountingAssignmentsManager() {
		return accountingAssignmentsManager;
	}
	*/
	
	
	protected static final long INACTIVITY_TIMEOUT = 10_000L;
	protected static final long HEARTBEAT_PERIOD = 3000L;
	
}
