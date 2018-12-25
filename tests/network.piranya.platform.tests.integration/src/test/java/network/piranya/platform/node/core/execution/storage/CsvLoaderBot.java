package network.piranya.platform.node.core.execution.storage;

import java.util.List;

import network.piranya.platform.api.extension_models.Parameters;
import network.piranya.platform.api.extension_models.execution.bots.Command;
import network.piranya.platform.api.extension_models.execution.bots.CommandBot;
import network.piranya.platform.api.lang.Result;
import network.piranya.platform.api.lang.ResultHandler;
import network.piranya.platform.api.models.infrastructure.CsvReaderParams;

public class CsvLoaderBot extends CommandBot {
	
	@Command(id = "load_csv")
	public void loadCsvFile(Parameters params, ResultHandler<String[]> resultHandler) {
		List<String> result = utils.col.list();
		context().storage().files().processSystemFile(params.string("path"), csvFile -> context().serialization().readCsv(csvFile, new CsvReaderParams(',', '"', false), fields -> {
			result.add(fields[0]);
		}));
		resultHandler.accept(new Result<>(result.toArray(new String[0])));
	}
	
}
