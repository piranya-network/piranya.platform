package network.piranya.platform.node.core.execution.analytics;

import static network.piranya.platform.node.utilities.CollectionUtils.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import network.piranya.platform.api.exceptions.PiranyaException;
import network.piranya.platform.api.extension_models.Parameters;
import network.piranya.platform.api.extension_models.analytics.AnalyticalQuery;
import network.piranya.platform.api.extension_models.analytics.AnalyticalView;
import network.piranya.platform.api.lang.OpenEndedPeriod;
import network.piranya.platform.api.lang.Period;
import network.piranya.platform.api.lang.Result;
import network.piranya.platform.api.lang.ResultHandler;
import network.piranya.platform.api.lang.TimeWindow;
import network.piranya.platform.api.models.analytics.AnalyticalViewData;
import network.piranya.platform.api.models.info.MarketInfoProvider;
import network.piranya.platform.api.models.log.ActivityLog;
import network.piranya.platform.node.api.local_infrastructure.LocalServices;
import network.piranya.platform.node.api.local_infrastructure.concurrency.Executor;
import network.piranya.platform.node.api.local_infrastructure.storage.KeyValueDb;
import network.piranya.platform.node.core.execution.engine.ActivityLogger;
import network.piranya.platform.node.utilities.DateTimeUtils;
import network.piranya.platform.node.utilities.TimeService;
import network.piranya.platform.node.utilities.Encoder;
import network.piranya.platform.node.utilities.ReflectionUtils;

public class AnalyticsEngine {
	
	@SuppressWarnings("unchecked")
	public <ViewDataType> void view(String viewId, String analyticalViewTypeId, boolean persistent, OpenEndedPeriod startPeriod, Parameters params,
			ResultHandler<AnalyticalViewData<ViewDataType>> resultHandler) {
		View view = viewsMap.get(viewId);
		if (view == null) {
			view = createView(viewId, analyticalViewTypeId, startPeriod, params);
			viewsMap.put(viewId, view);
			//db().put(key(viewId), serialize(new AnalyticsViewDetails(viewId, analyticalViewTypeId, startPeriod, params)));
			
			final View v = view;
			executor().execute(() -> {
				try {
					activityLogger().query(v.startTime(), TimeService.now(), log -> acceptLog(log, v));
					resultHandler.accept(new Result<>((AnalyticalViewData<ViewDataType>)v.analyticalView().data()));
				} catch (Exception ex) {
					LOG.warn(ex.getMessage(), ex);
					resultHandler.accept(new Result<>(ex));
				}
			});
		} else {
			resultHandler.accept(new Result<>((AnalyticalViewData<ViewDataType>)view.analyticalView().data()));
		}
	}
	
	@SuppressWarnings("unchecked")
	public <ViewDataType> AnalyticalViewData<ViewDataType> getView(String viewId) {
		View view = viewsMap.get(viewId);
		if (view != null) {
			return (AnalyticalViewData<ViewDataType>)view.analyticalView().data();
		} else {
			throw new PiranyaException(String.format("Analytical View '%s' is not registered", viewId));
		}
	}
	
	@SuppressWarnings("unchecked")
	public <ViewDataType> void query(String analyticalQueryTypeId, Period period, Parameters params, ResultHandler<ViewDataType> resultHandler) {
		AnalyticalQuery query = createQuery(analyticalQueryTypeId, params);
		executor().execute(() -> {
			try {
				activityLogger().query(period.startTime(), period.endTime(), log -> {
					if ((log.getTime() >= period.startTime() && log.getTime() <= period.endTime()) && query.filter(log)) {
						query.accept(log);
					}
				});
				resultHandler.accept(new Result<>((ViewDataType)query.getQueryResult()));
			} catch (Exception ex) {
				LOG.warn(ex.getMessage(), ex);
				resultHandler.accept(new Result<>(ex));
			}
		});
	}
	
	
	protected void onActivityLog(ActivityLog log) {
		executor().execute(() -> foreach(views(), view -> acceptLog(log, view)));
	}
	
	protected void acceptLog(ActivityLog log, View view) {
		view.accept(log);
	}
	
	
	@SuppressWarnings("unchecked")
	public <ViewType extends AnalyticalView> void registerViewType(Class<ViewType> viewType) {
		viewTypes().put(viewType.getName(), (Class<AnalyticalView>)viewType);
	}
	
	public <ViewType extends AnalyticalView> void deregisterViewType(Class<ViewType> viewType) {
		viewTypes().remove(viewType.getName());
	}
	@SuppressWarnings("unchecked")
	
	public <QueryType extends AnalyticalQuery> void registerQueryType(Class<QueryType> queryType) {
		queryTypes().put(queryType.getName(), (Class<AnalyticalQuery>)queryType);
	}
	
	public <QueryType extends AnalyticalQuery> void deregisterQueryType(Class<QueryType> queryType) {
		queryTypes().remove(queryType.getName());
	}
	
	protected View createView(String viewId, String analyticalViewTypeId, OpenEndedPeriod startPeriod, Parameters params) {
		Class<AnalyticalView> viewType = viewTypes().get(analyticalViewTypeId);
		if (viewType == null) {
			throw new PiranyaException(String.format("Analytical View Type '%s' is not registered", analyticalViewTypeId));
		}
		
		AnalyticalView view = ReflectionUtils.createInstance(viewType);
		view.init(params, marketInfoProvider());
		return new View(view, startPeriod);
	}
	
	protected AnalyticalQuery createQuery(String analyticalQueryTypeId, Parameters params) {
		Class<AnalyticalQuery> viewType = queryTypes().get(analyticalQueryTypeId);
		if (viewType == null) {
			throw new PiranyaException(String.format("Analytical View Type '%s' is not registered", analyticalQueryTypeId));
		}
		
		AnalyticalQuery query = ReflectionUtils.createInstance(viewType);
		query.init(params, marketInfoProvider());
		return query;
	}
	
	protected ByteBuffer key(String viewId) {
		byte[] bytes = (PREFIX + viewId).getBytes();
		ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length);
		return buffer.put(bytes).flip();
	}
	
	protected ByteBuffer serialize(AnalyticsViewDetails viewDetails) {
		byte[] bytes = encoder().encode(viewDetails);
		ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length);
		return buffer.put(bytes).flip();
	}
	
	
	protected void checkViews() {
		long now = TimeService.now();
		
		foreach(views(), v -> v.updateStartTime(now));
		
		filter(views(), v -> now > v.refreshTime()); // TODO ///////////////////////////////////
	}
	
	
	public void init() {
		loadViews();
		
		if (!views().isEmpty()) {
			SortedSet<Long> startTimes = new TreeSet<>(map(views(), v -> v.startTime()));
			activityLogger().query(startTimes.first(), TimeService.now(), log -> onActivityLog(log));
		}
		
		activityLogger().subscribe(activityLoggerSubscriber);
		
		executor().scheduleAtFixedRate(this::checkViews, 1000L, 1000L, TimeUnit.MILLISECONDS);
	}
	
	protected void loadViews() {
		List<AnalyticsViewDetails> views = new ArrayList<>();
		db().iterate(PREFIX, (keyBuffer, valueBuffer) -> {
			byte[] data = new byte[valueBuffer.remaining()];
			valueBuffer.get(data);
			views.add(encoder().decode(data));
		});
		
		foreach(views, v -> viewsMap.put(v.viewId(), createView(v.viewId(), v.viewTypeId(), v.startPeriod(), v.params())));
	}
	
	public void dispose() {
		activityLogger().unsubscribe(activityLoggerSubscriber);
		
		executor().dispose();
	}
	
	
	protected int maxPastEntries() {
		return 500;
	}
	
	
	public AnalyticsEngine(ActivityLogger activityLogger, LocalServices localServices, MarketInfoProvider marketInfoProvider) {
		this.activityLogger = activityLogger;
		this.marketInfoProvider = marketInfoProvider;
		this.executor = localServices.separateExecutor(new Executor.Config(1));
		this.encoder = new Encoder();
		this.encoder.registerDataType(AnalyticsViewDetails.class, (short)0);
		this.db = localServices.localStorage().cacheDb();
	}
	
	private final ActivityLogger activityLogger;
	protected ActivityLogger activityLogger() { return activityLogger; }
	
	private final MarketInfoProvider marketInfoProvider;
	protected MarketInfoProvider marketInfoProvider() { return marketInfoProvider; }
	
	private final Executor executor;
	protected Executor executor() { return executor; }
	
	private final ConcurrentMap<String, View> viewsMap = new ConcurrentHashMap<>();
	protected Collection<View> views() { return viewsMap.values(); }
	
	private final Encoder encoder;
	protected Encoder encoder() { return encoder; }
	
	private final KeyValueDb db;
	protected KeyValueDb db() { return db; }
	
	private final ConcurrentMap<String, Class<AnalyticalView>> viewTypes = new ConcurrentHashMap<>();
	protected ConcurrentMap<String, Class<AnalyticalView>> viewTypes() { return viewTypes; }
	
	private final ConcurrentMap<String, Class<AnalyticalQuery>> queryTypes = new ConcurrentHashMap<>();
	protected ConcurrentMap<String, Class<AnalyticalQuery>> queryTypes() { return queryTypes; }
	
	private final Consumer<ActivityLog> activityLoggerSubscriber = this::onActivityLog;
	
	
	protected class View {
		
		public void updateStartTime(long now) {
			if (now > currentWindow.endTime()) {
				currentWindow = DateTimeUtils.calcWindowStartAndEndTime(now, startPeriod().unit(), startPeriod().value());
			}
			
			while (previousLogs().size() > 0 && currentWindow.startTime() > previousLogs().get(0).getTime()) {
				analyticalView().forget(previousLogs().remove(0));
			}
		}
		
		public void accept(ActivityLog log) {
			if (startPeriod().unit() != null) {
				updateStartTime(log.getTime());
			}
			
			if (log.getTime() >= currentWindow.startTime() && analyticalView().filter(log)) {
				if (previousLogs().size() < maxPastEntries()) {
					previousLogs().add(log);
				} else {
					refreshTime = currentWindow.endTime();
					previousLogs().clear();
				}
				
				analyticalView().accept(log);
			}
		}
		
		public View(AnalyticalView analyticalView, OpenEndedPeriod startPeriod) {
			this.analyticalView = analyticalView;
			this.startPeriod = startPeriod;
			this.currentWindow = startPeriod().unit() != null
					? DateTimeUtils.calcWindowStartAndEndTime(TimeService.now(), startPeriod().unit(), startPeriod().value())
					: new TimeWindow(startPeriod().value(), Long.MAX_VALUE);
		}
		
		private final AnalyticalView analyticalView;
		public AnalyticalView analyticalView() { return analyticalView; }
		
		private final OpenEndedPeriod startPeriod;
		protected OpenEndedPeriod startPeriod() { return startPeriod; }
		
		private TimeWindow currentWindow;
		
		private final List<ActivityLog> previousLogs = new ArrayList<>();
		protected List<ActivityLog> previousLogs() { return previousLogs; }
		
		public long startTime() {
			return currentWindow.startTime();
		}
		
		public long refreshTime() {
			return refreshTime;
		}
		
		private long refreshTime = Long.MAX_VALUE;
	}
	
	private static final byte[] PREFIX = "ae.views#".getBytes();
	
	private static final Logger LOG = LoggerFactory.getLogger(AnalyticsEngine.class);
	
}
