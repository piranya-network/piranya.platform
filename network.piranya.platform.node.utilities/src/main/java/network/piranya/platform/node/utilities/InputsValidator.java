package network.piranya.platform.node.utilities;

import java.util.List;

import network.piranya.platform.api.exceptions.InvalidParameterException;
import network.piranya.platform.api.extension_models.Input;
import network.piranya.platform.api.extension_models.Parameters;

public class InputsValidator {
	
	public void validate(Parameters params, Input[] inputs) {
		for (Input input : inputs) {
			int minEntries = input.minEntries();
			int maxEntries = input.maxEntries() > input.minEntries() ? input.maxEntries() : input.minEntries();
			if (maxEntries > 1) {
				/// if not list an exception will be thrown
				List<String> list = params.list(input.id());
				if (list.size() < minEntries) {
					throw new InvalidParameterException(String.format("'%s' must have at least %s entries", input.id(), minEntries), input.id(), false);
				} else if (list.size() > maxEntries) {
					throw new InvalidParameterException(String.format("'%s' must have maximum %s entries", input.id(), maxEntries), input.id(), false);
				}
			}
			
			// check enum
			
			// check constraints
		}
	}
	
}
