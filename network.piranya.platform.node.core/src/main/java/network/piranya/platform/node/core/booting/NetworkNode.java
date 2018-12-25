package network.piranya.platform.node.core.booting;

import java.util.function.BiConsumer;

import network.piranya.infrastructure.dcm4j.api.ComponentContext;
import network.piranya.infrastructure.dcm4j.api.ComponentModel;
import network.piranya.platform.api.accounting.NetworkCredentials;
import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.api.lang.ReplyHandler;
import network.piranya.platform.node.accounts.Account;
import network.piranya.platform.node.accounts.security.CertificationInfo;
import network.piranya.platform.node.api.booting.NetworkNodeConfig;
import network.piranya.platform.node.api.execution.liquidity.LiquidityProvidersRegistry;
import network.piranya.platform.node.api.local_infrastructure.LocalServices;
import network.piranya.platform.node.api.local_infrastructure.LocalServicesProvider;
import network.piranya.platform.node.api.networking.nodes.LocalNodeConfig;
import network.piranya.platform.node.core.execution.ExecutionManagerImpl;
import network.piranya.platform.node.core.execution.bots.BotsRegistryImpl;
import network.piranya.platform.node.core.execution.liquidity.LiquidityProvidersRegistryImpl;
import network.piranya.platform.node.core.info.MarketInfoProviderImpl;
import network.piranya.platform.node.core.modules.ModulesManagerImpl;
import network.piranya.platform.node.core.modules.app.UiComponentsRegistryImpl;
import network.piranya.platform.node.core.networks.index.client.IndexClient;
import network.piranya.platform.node.core.networks.index.cluster.IndexCluster;
import network.piranya.platform.node.core.networks.storage.network.DataStorageNetwork;
import network.piranya.platform.node.core.search.DefaultSearchService;
import network.piranya.platform.node.utilities.impl.ReplyHandlerImpl;

// fast implementation of connecting/followup, providing services
public class NetworkNode {
	
	public ReplyHandler<NetworkNode> boot() {
		/**Plain English:
		 * Connect to Index nodes and ask them to sign in. On successful reply, boot our node.
		 */
		ReplyHandlerImpl<NetworkNode> replyHandler = new ReplyHandlerImpl<>();
		
		if (signInOnBooting) {
			Optional<Account> account = localServices().localStorage().accountsLocalDb().findAccount(credentials().accountRef());
			boolean isIndexNode = account.isPresent() && account.get().isCertified(CertificationInfo.INDEX_NODE);
			if (isIndexNode) {
				System.out.println("index node");
				bootAccountNode(account.get());
				bootIndexCluster();
				replyHandler.doReply(this);
			} else {
				indexClient().signin(credentials())
					.onReply(reply -> {
						bootAccountNode(reply.account());
						replyHandler.doReply(this);
					})
					.onError(error -> replyHandler.doError(error));
			}
		} else {
			bootAccountNode(null);
			replyHandler.doReply(this);
		}
		
		return replyHandler;
	}
	
	/// for now, connecting to network is not enabled
	private boolean signInOnBooting = false;
	
	protected boolean isIndexNode() {
		Optional<Account> account = localServices().localStorage().accountsLocalDb().findAccount(credentials().accountRef());
		return account.isPresent() && account.get().isCertified(CertificationInfo.INDEX_NODE);
	}
	
	public void shutdown() {
		marketInfoProvider().dispose();
	}
	
	
	protected void bootAccountNode(Account account) {
		//setAccountingActivitiesManager(new AccountingActivitiesManager(config(), localServicesProvider(), reply));
		
		setDataStorageNetwork(new DataStorageNetwork(config(), localServicesProvider()));
		
		BotsRegistryImpl botsRegistry = new BotsRegistryImpl(localServices().localStorage());
		LiquidityProvidersRegistryImpl liquidityProvidersRegistry = new LiquidityProvidersRegistryImpl(
				() -> executionManager().localExecutionEngine(), (instrument, lpId) -> marketInfoProvider.registerInstrument(instrument, lpId), localServices().localStorage());
		
		setUiComponentsRegistry(new UiComponentsRegistryImpl());
		
		setSearchService(new DefaultSearchService(botsRegistry, uiComponentsRegistry(), liquidityProvidersRegistry, marketInfoProvider()));
		
		setExecutionManager(new ExecutionManagerImpl(config(), localServicesProvider(), botsRegistry, liquidityProvidersRegistry, searchService(), marketInfoProvider()));
		
		setModulesManager(new ModulesManagerImpl(botsRegistry, liquidityProvidersRegistry, executionManager(), uiComponentsRegistry(), componentContext(), appEventsPublisher()));
		modulesManager().initPreExecutionInit();
		
		executionManager().init();
		
		modulesManager().initPostExecutionInit();
		
		liquidityProvidersRegistry.loadStoredLps();
		
		/*
		if (account.isCertified(CertificationInfo.EXECUTION_NODE)) {
			// participate? open ports? , relocate this logic to execution node code itself?
		}
		*/
		
		// setGlobalServicesHost(new GlobalServicesHost())
	}
	
	protected void bootIndexCluster() {
		indexCluster().ifPresent(cluster -> cluster.shutdown());
		
		setIndexCluster(new IndexCluster(credentials(), localServicesProvider(), this::bootIndexCluster));
		indexCluster().get().boot();
	}
	
	
	public NetworkNode(NetworkCredentials credentials, NetworkNodeConfig config, LocalServicesProvider localServicesProvider,
			ComponentModel componentModel, BiConsumer<String, Object> appEventsPublisher) {
		this.componentContext = componentModel.createComponentContext(String.format("NetworkNode:%s", config.nodeId()));
		this.credentials = credentials;
		this.config = config;
		this.localServicesProvider = localServicesProvider;
		this.localServices = localServicesProvider.services(this);
		this.indexClient = new IndexClient(config, new LocalNodeConfig(localServices().localStorage()), localServicesProvider);
		this.marketInfoProvider = new MarketInfoProviderImpl(localServicesProvider);
		this.appEventsPublisher = appEventsPublisher;
	}
	
	/*
	private AccountingActivitiesManager accountingActivitiesManager;
	protected AccountingActivitiesManager accountingActivitiesManager() {
		return accountingActivitiesManager;
	}
	protected void setAccountingActivitiesManager(AccountingActivitiesManager accountingActivitiesManager) {
		this.accountingActivitiesManager = accountingActivitiesManager;
	}
	*/
	private final ComponentContext componentContext;
	protected ComponentContext componentContext() {
		return componentContext;
	}
	
	private final NetworkCredentials credentials;
	protected NetworkCredentials credentials() {
		return credentials;
	}
	
	private final NetworkNodeConfig config;
	protected NetworkNodeConfig config() {
		return config;
	}
	
	private final LocalServicesProvider localServicesProvider;
	protected LocalServicesProvider localServicesProvider() {
		return localServicesProvider;
	}
	
	private final LocalServices localServices;
	protected LocalServices localServices() {
		return localServices;
	}
	
	private BiConsumer<String, Object> appEventsPublisher;
	protected BiConsumer<String, Object> appEventsPublisher() { return appEventsPublisher; }
	
	private final IndexClient indexClient;
	protected IndexClient indexClient() {
		return indexClient;
	}
	
	private Optional<IndexCluster> indexCluster = Optional.empty();
	protected Optional<IndexCluster> indexCluster() {
		return indexCluster;
	}
	protected void setIndexCluster(IndexCluster indexCluster) {
		this.indexCluster = Optional.of(indexCluster);
	}
	
	private DataStorageNetwork dataStorageNetwork;
	protected DataStorageNetwork dataStorageNetwork() {
		return dataStorageNetwork;
	}
	protected void setDataStorageNetwork(DataStorageNetwork dataStorageNetwork) {
		this.dataStorageNetwork = dataStorageNetwork;
	}
	
	private final MarketInfoProviderImpl marketInfoProvider;
	protected MarketInfoProviderImpl marketInfoProvider() {
		return marketInfoProvider;
	}
	
	private DefaultSearchService searchService;
	protected DefaultSearchService searchService() {
		return searchService;
	}
	protected void setSearchService(DefaultSearchService searchService) {
		this.searchService = searchService;
	}
	
	private ExecutionManagerImpl executionManager;
	public ExecutionManagerImpl executionManager() {
		return executionManager;
	}
	protected void setExecutionManager(ExecutionManagerImpl executionManager) {
		this.executionManager = executionManager;
	}
	
	private UiComponentsRegistryImpl uiComponentsRegistry;
	protected UiComponentsRegistryImpl uiComponentsRegistry() {
		return uiComponentsRegistry;
	}
	protected void setUiComponentsRegistry(UiComponentsRegistryImpl uiComponentsRegistry) {
		this.uiComponentsRegistry = uiComponentsRegistry;
	}
	
	public LiquidityProvidersRegistry liquidityProvidersRegistry() {
		return modulesManager().liquidityProvidersRegistry();
	}
	
	private ModulesManagerImpl modulesManager;
	public ModulesManagerImpl modulesManager() {
		return modulesManager;
	}
	protected void setModulesManager(ModulesManagerImpl modulesManager) {
		this.modulesManager = modulesManager;
	}
	
}
