package network.piranya.platform.api.lang;

import network.piranya.platform.api.lang.Optional;

public class Result<ResultType> {
	
	public boolean isSuccessful() {
		return !error().isPresent();
	}
	
	private final Optional<ResultType> result;
	public Optional<ResultType> result() {
		return result;
	}
	
	private final Optional<Exception> error;
	public Optional<Exception> error() {
		return error;
	}
	
	public Result(ResultType result) {
		this.result = Optional.of(result);
		this.error = Optional.empty();
	}
	
	public Result(Exception error) {
		this.result = Optional.empty();
		this.error = Optional.of(error);
	}
	
}
