package network.piranya.platform.api.lang;

import java.util.function.Consumer;

import network.piranya.platform.api.exceptions.TimeoutException;

public interface ResultFuture<ResultType> {
	
	ResultFuture<ResultType> onSuccess(Consumer<ResultType> resultHandler);
	ResultFuture<ResultType> onFailure(Consumer<Exception> errorHandler);
	
	ResultType waitForResult(int timeout) throws TimeoutException, Exception;
	
}
