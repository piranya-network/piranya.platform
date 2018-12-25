package network.piranya.platform.node.core.search;

import static network.piranya.platform.node.utilities.CollectionUtils.*;
import static network.piranya.platform.node.utilities.MathUtils.*;
import static network.piranya.platform.node.utilities.StringUtils.*;
import static network.piranya.platform.node.utilities.StringUtils.splitToCamelCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import network.piranya.platform.api.extension_models.ActionType;
import network.piranya.platform.api.extension_models.app.ui.UiAction;
import network.piranya.platform.api.extension_models.execution.bots.ExecutionBot;
import network.piranya.platform.api.extension_models.execution.liquidity.LiquidityProvider;
import network.piranya.platform.api.extension_models.execution.liquidity.LiquidityProviderCategory;
import network.piranya.platform.api.models.bots.BotView;
import network.piranya.platform.api.models.info.AssetInfo;
import network.piranya.platform.api.models.info.MarketInfoProvider;
import network.piranya.platform.api.models.metadata.BotTypeInfo;
import network.piranya.platform.api.models.metadata.CommandInfo;
import network.piranya.platform.api.models.metadata.LiquidityProviderTypeInfo;
import network.piranya.platform.api.models.search.SearchAction;
import network.piranya.platform.api.models.search.SearchQuery;
import network.piranya.platform.api.models.search.SearchResult;
import network.piranya.platform.api.models.search.SearchResultEntry;
import network.piranya.platform.api.models.search.SearchService;
import network.piranya.platform.api.models.trading.Instrument;
import network.piranya.platform.node.api.app.ui.PageDetails;
import network.piranya.platform.node.api.app.ui.UiComponentsRegistry;
import network.piranya.platform.node.api.execution.bots.BotsRegistry;
import network.piranya.platform.node.api.execution.liquidity.LiquidityProvidersRegistry;

public class DefaultSearchService implements SearchService {
	
	@Override
	public void search(SearchQuery query, Consumer<SearchResult> resultHandler) {
		try {
			Set<String> tokens = toSet(query.getQueryText().toLowerCase().split("\\s"));
			String concatedTokens = concatStringCollection(tokens, "-", false);
			
			List<Contestant<AssetInfo>> assets = map(marketInfoProvider().findAssets(query.getQueryText()).waitForResult(1000), a -> new Contestant<>(a, 1.0));
			List<Contestant<Instrument>> instruments = map(marketInfoProvider().findInstruments(query.getQueryText()).waitForResult(1000), a -> new Contestant<>(a, 1.0));
			
			// if "bot" word in query, exclude it maybe and limit search to bots
			boolean noFeaturesRequired = query.getFeatures().isEmpty();
			List<Contestant<BotTypeInfo>> filteredBotTypes = filter(map(map(botsRegistry().botTypes(), botType -> botsRegistry().botMetadata(botType.getName())),
					b -> new Contestant<>(b, max(featuresScore(tokens, concatedTokens, b), max(tagsScore(tokens, concatedTokens, b.getSearchInfo().getTags()),
							titleScore(tokens, concatedTokens, replace(b.getDisplayName(), " ", "-").toLowerCase(), b.getDescription()))))),
					c -> c.score() > 0.0 && (noFeaturesRequired || containsAnyFeature(c.component().getFeatures(), query.getFeatures())));
			
			List<Contestant<PageDetails>> filteredPages = filter(map(uiComponentsRegistry().listPages(),
					p -> new Contestant<>(p, titleScore(tokens, concatedTokens, splitToCamelCase(p.page().title(), "-").toLowerCase(), ""))), c -> c.score() > 0.0);
			
			List<Contestant<UiAction>> filteredUiActions = filter(map(uiComponentsRegistry().listUiActions(),
					a -> new Contestant<>(a, max(featuresScore(tokens, concatedTokens, a.features()), max(tagsScore(tokens, concatedTokens, a.searchTags()),
							titleScore(tokens, concatedTokens, splitToCamelCase(a.title(), "-").toLowerCase(), ""))))),
					c -> c.score() > 0.0 && (noFeaturesRequired || containsAnyFeature(c.component().features(), query.getFeatures())));
			
			List<Contestant<ExecutioBotItem>> filteredBots = map(filter(botsRegistry().botsList(),b -> find(filteredBotTypes,
					bt -> botsRegistry().botType(bt.component().getBotTypeId()).isInstance(b)).isPresent() || b.view().label().toLowerCase().contains(query.getQueryText().toLowerCase())),
					b -> new Contestant<>(new ExecutioBotItem(b.view(), botsRegistry().botMetadata(b.getClass().getName())), 1.0));
			
			List<Contestant<BotTypeInfo>> commandBotTypes = filter(filteredBotTypes, c -> !ExecutionBot.class.isAssignableFrom(botsRegistry().botType(c.component().getBotTypeId())));
			List<Contestant<BotTypeInfo>> executionBotTypes = filter(filteredBotTypes, c -> ExecutionBot.class.isAssignableFrom(botsRegistry().botType(c.component().getBotTypeId())));
			
			List<Contestant<LiquidityProviderTypeInfo>> filteredLpTypes = filter(map(lpRegistry().liquidityProviderTypesInfo(),
					a -> new Contestant<>(a, max(featuresScore(tokens, concatedTokens, a.getFeatures()), max(tagsScore(tokens, concatedTokens, a.getSearchTags()),
							titleScore(tokens, concatedTokens, splitToCamelCase(a.getDisplayName(), "-").toLowerCase(), ""))))),
					c -> c.score() > 0.0 && (noFeaturesRequired || containsAnyFeature(c.component().getFeatures(), query.getFeatures())));
			
			List<Contestant<LiquidityProvider>> filteredLps = filter(map(lpRegistry().liquidityProviders(),
					a -> new Contestant<>(a, titleScore(tokens, concatedTokens, a.liquidityProviderId().toLowerCase(), ""))),
					c -> c.score() > 0.0);
			
			// assets first. show only 1 asset
			// instruments
			// then LP
			//SortedSet<SearchEntryContestant> entryContestants = new TreeSet<>();
			List<SearchEntryContestant> entryContestants = new ArrayList<>();
			// TODO relocate checks for lookIn condition to before filtering occurs
			boolean lookInAll = query.getLookIn().isEmpty();
			if (lookInAll || query.getLookIn().contains(SearchQuery.LookIn.COMMAND_BOT_TYPES)) {
				entryContestants.addAll(map(commandBotTypes, bt -> new SearchEntryContestant(
						new SearchResultEntry(bt.component().getDisplayName(), SearchResultEntry.EntryType.BOT, bt.component().getDescription(), map(toList(bt.component().getCommands()),
								c -> new SearchAction(c.getDisplayName(), ActionType.RUN_BOT,
										String.format("%s!%s/%s", ActionType.RUN_BOT.prefix(), bt.component().getBotTypeId(), c.getCommandId()))).toArray(new SearchAction[0])), bt.score())));
			}
			if (lookInAll || query.getLookIn().contains(SearchQuery.LookIn.EXECUTION_BOT_TYPES)) {
				entryContestants.addAll(map(executionBotTypes, bt -> new SearchEntryContestant(
						new SearchResultEntry(bt.component().getDisplayName(), SearchResultEntry.EntryType.BOT, bt.component().getDescription(), new SearchAction[] {
								new SearchAction("Run", ActionType.RUN_BOT, String.format("%s!%s", ActionType.RUN_BOT.prefix(), bt.component().getBotTypeId())) }), bt.score())));
			}
			if (lookInAll || query.getLookIn().contains(SearchQuery.LookIn.PAGES)) {
				entryContestants.addAll(map(filteredPages, p -> new SearchEntryContestant(
						new SearchResultEntry(p.component().page().title(), SearchResultEntry.EntryType.PAGE, "Page", new SearchAction[] {
								new SearchAction("Open", ActionType.GOTO_PAGE, String.format("%s!%s", ActionType.GOTO_PAGE.prefix(), p.component().page().id())) }), p.score())));
			}
			if (lookInAll || query.getLookIn().contains(SearchQuery.LookIn.RUNNING_BOTS)) {
				entryContestants.addAll(map(filteredBots, b -> new SearchEntryContestant(
						new SearchResultEntry(b.component().view.label(), SearchResultEntry.EntryType.BOT, b.component().view.description(), map(toList(b.component().botTypeInfo.getCommands()),
								c -> new SearchAction(c.getDisplayName(), ActionType.RUN_BOT,
										String.format("%s!%s/%s", ActionType.RUN_BOT.prefix(), b.component().view.ref().botId(), c.getCommandId()))).toArray(new SearchAction[0])), b.score())));
			}
			if (lookInAll || query.getLookIn().contains(SearchQuery.LookIn.INSTRUMENTS)) {
				entryContestants.addAll(map(instruments, a -> new SearchEntryContestant(
						new SearchResultEntry(a.component().symbol(), SearchResultEntry.EntryType.INSTRUMENT, String.format("Instrument: %s",
								a.component().symbol()), new SearchAction[] {
								new SearchAction("View Exchanges", ActionType.UI_ACTION, String.format("%s!%s|%s",
										ActionType.UI_ACTION.prefix(), "view_exchanges_for_instrument", a.component().symbol()))}), a.score())));
			}
			if (lookInAll || query.getLookIn().contains(SearchQuery.LookIn.ASSETS)) {
				entryContestants.addAll(map(assets, a -> new SearchEntryContestant(
						new SearchResultEntry(a.component().symbol(), SearchResultEntry.EntryType.ASSET, String.format("Asset: %s - %s",
								a.component().symbol(), a.component().displayName()), new SearchAction[] {
								new SearchAction("View Pairs", ActionType.UI_ACTION, String.format("%s!%s|%s",
										ActionType.UI_ACTION.prefix(), "view_instruments_for_asset", a.component().symbol())),
								new SearchAction("View Exchanges", ActionType.UI_ACTION, String.format("%s!%s|%s",
										ActionType.UI_ACTION.prefix(), "view_exchanges_for_asset", a.component().symbol()))}), a.score())));
			}
			if (lookInAll || query.getLookIn().contains(SearchQuery.LookIn.UI_ACTIONS)) {
				entryContestants.addAll(map(filteredUiActions, p -> new SearchEntryContestant(
						new SearchResultEntry(p.component().title(), SearchResultEntry.EntryType.UI_ACTION, p.component().description(), new SearchAction[] {
								new SearchAction(p.component().launchLabel(), ActionType.GOTO_PAGE,
										String.format("%s!%s", ActionType.UI_ACTION.prefix(), p.component().actionId())) }), p.score())));
			}
			if (lookInAll || query.getLookIn().contains(SearchQuery.LookIn.AVAILABLE_LP)) {
				entryContestants.addAll(map(filteredLpTypes, bt -> {
					SearchResultEntry.EntryType entryType = bt.component().getLpCategory() == LiquidityProviderCategory.DECENTRALIZED
							? SearchResultEntry.EntryType.LIQUIDITY_PROVIDER_DEX : SearchResultEntry.EntryType.LIQUIDITY_PROVIDER;
					return new SearchEntryContestant(new SearchResultEntry(bt.component().getDisplayName(), entryType, bt.component().getDescription(), new SearchAction[] {
							new SearchAction("Register", ActionType.REGISTER_LP, String.format("%s!%s", ActionType.REGISTER_LP.prefix(), bt.component().getLpTypeId())) }),
							bt.score());
					}));
			}
			if (lookInAll || query.getLookIn().contains(SearchQuery.LookIn.REGISTERED_LP)) {
				entryContestants.addAll(map(filteredLps, lp -> new SearchEntryContestant(new SearchResultEntry(lp.component().liquidityProviderId(),
						SearchResultEntry.EntryType.LIQUIDITY_PROVIDER, "", new SearchAction[] {
								new SearchAction("Connect", ActionType.RUN_BOT, String.format("%s!%s/%s|lp_id=%s", ActionType.RUN_BOT.prefix(),
										"network.piranya.extensions.core_ui.core.extensions.app.command_bots.LpCommandingBot", "connect_lp", lp.component().liquidityProviderId())),
								new SearchAction("Disconnect", ActionType.RUN_BOT, String.format("%s!%s/%s|lp_id=%s", ActionType.RUN_BOT.prefix(),
										"network.piranya.extensions.core_ui.core.extensions.app.command_bots.LpCommandingBot", "disconnect_lp", lp.component().liquidityProviderId())),
								new SearchAction("Remove", ActionType.RUN_BOT, String.format("%s!%s/%s|lp_id=%s", ActionType.RUN_BOT.prefix(),
										"network.piranya.extensions.core_ui.core.extensions.app.command_bots.LpCommandingBot", "remove_lp", lp.component().liquidityProviderId())) }),
						lp.score())));
			}
			
			resultHandler.accept(new SearchResult(limit(map(entryContestants, c -> c.entry()), searchEntriesSizeLimit()).toArray(new SearchResultEntry[0])));
		} catch (Exception ex) {
			ex.printStackTrace();
			resultHandler.accept(new SearchResult(new SearchResultEntry[0]));
		}
	}
	
	protected boolean containsAnyFeature(String[] features, Set<String> required) {
		for (String feature : features) {
			if (required.contains(feature)) {
				return true;
			}
		}
		return false;
	}
	
	protected class ExecutioBotItem {
		
		public BotView view;
		public BotTypeInfo botTypeInfo;

		public ExecutioBotItem(BotView view, BotTypeInfo botTypeInfo) {
			this.view = view;
			this.botTypeInfo = botTypeInfo;
		}
	}
	
	
	protected double tagsScore(Set<String> tokens, String concatedTokens, String[] tags) {
		Set<String> tagsSet = toSet(tags);
		if (tagsSet.contains(concatedTokens)) {
			return 1.0;
		}
		
		double max = 0.0;
		for (String tag : tags) {
			if (tokens.contains(tag)) {
				if (0.8 > max) {
					max = 0.8;
				}
			}
		}
		return max;
	}
	
	protected double featuresScore(Set<String> tokens, String concatedTokens, BotTypeInfo botTypeInfo) {
		double max = featuresScore(tokens, concatedTokens, botTypeInfo.getFeatures());
		for (CommandInfo commandInfo : botTypeInfo.getCommands()) {
			double score = featuresScore(tokens, concatedTokens, commandInfo.getFeatures());
			if (score > max) {
				max = score;
			}
		}
		return max;
	}
	
	protected double featuresScore(Set<String> tokens, String concatedTokens, String[] features) {
		Set<String> tagsSet = toSet(features);
		if (tagsSet.contains(concatedTokens)) {
			return 1.0;
		}
		
		double max = 0.0;
		for (String feature : features) {
			if (feature.contains(concatedTokens)) {
				if (0.8 > max) {
					max = 0.8;
				}
			} else {
				for (String token : tokens) {
					if (feature.contains(token)) {
						if (0.7 > max) {
							max = 0.7;
						}
					}
				}
			}
		}
		return max;
	}
	
	protected double titleScore(Set<String> tokens, String concatedTokens, String title, String description) {
		if (title.equals(concatedTokens)) {
			return 1.0;
		}
		
		for (String token : tokens) {
			if (title.contains(token)) {
				return 0.8;
			}
		}
		
		for (String token : tokens) {
			if (description.contains(token)) {
				return 0.6;
			}
		}
		
		return 0;
	}
	
	protected int searchEntriesSizeLimit() {
		return 10;
	}
	
	
	public DefaultSearchService(BotsRegistry botsRegistry, UiComponentsRegistry uiComponentsRegistry, LiquidityProvidersRegistry lpRegistry, MarketInfoProvider marketInfoProvider) {
		this.botsRegistry = botsRegistry;
		this.uiComponentsRegistry = uiComponentsRegistry;
		this.lpRegistry = lpRegistry;
		this.marketInfoProvider = marketInfoProvider;
	}
	
	private final BotsRegistry botsRegistry;
	protected BotsRegistry botsRegistry() { return botsRegistry; }
	
	private final UiComponentsRegistry uiComponentsRegistry;
	protected UiComponentsRegistry uiComponentsRegistry() { return uiComponentsRegistry; }
	
	private final LiquidityProvidersRegistry lpRegistry;
	protected LiquidityProvidersRegistry lpRegistry() { return lpRegistry; }
	
	private final MarketInfoProvider marketInfoProvider;
	protected MarketInfoProvider marketInfoProvider() { return marketInfoProvider; }
	
	
	protected class Contestant<T> {
		
		private final T component;
		public T component() { return component; }
		
		private final double score;
		public double score() { return score; }
		
		public Contestant(T component, double score) {
			this.component = component;
			this.score = score;
		}
	}
	
	protected class SearchEntryContestant implements Comparable<SearchEntryContestant> {
		
		private final SearchResultEntry entry;
		public SearchResultEntry entry() { return entry; }
		
		private final double score;
		public double score() { return score; }
		
		public SearchEntryContestant(SearchResultEntry entry, double score) {
			this.entry = entry;
			this.score = score;
		}

		@Override
		public int compareTo(SearchEntryContestant o) {
			return difference(Double.compare(score(), o.score()), entry().getTitle().compareTo(o.entry().getTitle()));
		}
	}
	
}
