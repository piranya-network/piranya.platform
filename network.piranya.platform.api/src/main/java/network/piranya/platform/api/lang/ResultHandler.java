package network.piranya.platform.api.lang;

public interface ResultHandler<ResultType> {
	
	void accept(Result<ResultType> result);
	
}