package network.piranya.platform.node.app.services;

import java.util.List;

import network.piranya.platform.api.extension_models.InputType;
import network.piranya.platform.api.extension_models.Parameters;
import network.piranya.platform.api.extension_models.app.AppService;
import network.piranya.platform.api.extension_models.app.AppServiceMetadata;
import network.piranya.platform.api.extension_models.app.Operation;
import network.piranya.platform.api.lang.Result;
import network.piranya.platform.api.lang.ResultHandler;
import network.piranya.platform.api.models.search.SearchQuery;
import network.piranya.platform.api.models.search.SearchResult;

@AppServiceMetadata(id = "search")
public class SearchAppService extends AppService {
	
	@Operation("search")
	public void invoke(SearchQuery query, ResultHandler<SearchResult> resultHandler) {
		context().search().search(query, r -> resultHandler.accept(new Result<>(r)));
	}
	
	@Operation("search_instruments")
	public void searchInstruments(Parameters params, ResultHandler<List<String>> resultHandler) {
		context().marketInfo().findInstruments(params.string("keyword"), InputType.valueOf(params.string("type")))
				.onSuccess(result -> resultHandler.accept(new Result<>(utils.col.map(result, i -> i.symbol()))))
				.onFailure(error -> resultHandler.accept(new Result<>(error)));
	}
	
}
