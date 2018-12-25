package network.piranya.platform.api.extension_models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import network.piranya.platform.api.exceptions.InvalidParameterException;
import network.piranya.platform.api.models.metadata.InputInfo;

public class Parameters extends Data {
	
	public Parameters(Map<String, Object> data) {
		super(data);
	}
	
	
	public void validate(InputInfo[] inputs) {
		for (InputInfo input : inputs) {
			int minEntries = input.getMinEntries();
			int maxEntries = input.getMaxEntries() > input.getMinEntries() ? input.getMaxEntries() : input.getMinEntries();
			if (maxEntries > 1) {
				/// if not list an exception will be thrown
				List<String> list = list(input.getId());
				if (list.size() < minEntries) {
					throw new InvalidParameterException(String.format("'%s' must have at least %s entries", input.getId(), minEntries), input.getId(), false);
				} else if (list.size() > maxEntries) {
					throw new InvalidParameterException(String.format("'%s' must have maximum %s entries", input.getId(), maxEntries), input.getId(), false);
				}
			} else {
				if (minEntries == 1) {
					Object value = dataMap().get(input.getId());
					if (value == null || value.toString().trim().length() == 0) {
						throw new InvalidParameterException(String.format("Parameter '%s' is required", input.getId()), input.getId(), false);
					}
				}
			}
			
			// check enum
			
			// check constraints
		}
	}
	
	public void validate(Input[] inputs) {
		InputInfo[] inputsInfo = new InputInfo[inputs.length];
		for (int i = 0; i < inputs.length; i++) {
			inputsInfo[i] = new InputInfo(inputs[i]);
		}
		
		validate(inputsInfo);
	}
	
	
	public static final Parameters EMPTY = new Parameters(new HashMap<>());
	
}
