package network.piranya.platform.node.utilities.impl;

import java.util.function.Consumer;

import network.piranya.platform.api.exceptions.TimeoutException;
import network.piranya.platform.api.lang.ResultFuture;
import network.piranya.platform.node.utilities.WaitingUtils;

public class ResultFutureImpl<ResultType> implements ResultFuture<ResultType> {
	
	@Override
	public ResultFuture<ResultType> onSuccess(Consumer<ResultType> resultHandler) {
		if (result != null) resultHandler.accept(result);
		else this.resultHandler = resultHandler;
		return this;
	}
	
	@Override
	public ResultFuture<ResultType> onFailure(Consumer<Exception> errorHandler) {
		if (error != null) errorHandler.accept(error);
		else this.errorHandler = errorHandler;
		return this;
	}
	
	@Override
	public ResultType waitForResult(int timeout) throws TimeoutException, Exception {
		if (result != null) {
			return result;
		} else if (error != null) {
			throw error;
		} else {
			if (WaitingUtils.waitUntil(timeout, 5, () -> result != null || error != null)) {
				if (result != null) return result;
				else throw error;
			} else {
				throw new TimeoutException();
			}
		}
	}
	
	
	public void acceptSuccess(ResultType result) {
		this.result = result;
		if (resultHandler != null) resultHandler.accept(result);
	}
	
	public void acceptFailure(Exception error) {
		this.error = error;
		if (errorHandler != null) errorHandler.accept(error);
	}
	
	private ResultType result = null;
	private Exception error = null;
	private Consumer<ResultType> resultHandler = null;
	private Consumer<Exception> errorHandler = null;
	
	
	public ResultFutureImpl() {
	}
	
	public ResultFutureImpl(ResultType result) {
		acceptSuccess(result);
	}
	
	public ResultFutureImpl(Exception error) {
		acceptFailure(error);
	}
	
}
