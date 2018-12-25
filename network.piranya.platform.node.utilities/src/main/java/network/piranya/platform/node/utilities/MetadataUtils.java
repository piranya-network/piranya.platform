package network.piranya.platform.node.utilities;

import static network.piranya.platform.node.utilities.CollectionUtils.map;

import java.util.Arrays;

import network.piranya.platform.api.extension_models.Input;
import network.piranya.platform.api.extension_models.Action;
import network.piranya.platform.api.extension_models.InputMetadata;
import network.piranya.platform.api.models.metadata.InputActionInfo;
import network.piranya.platform.api.models.metadata.InputInfo;

public class MetadataUtils {
	
	public static InputInfo[] getInputsInfo(InputMetadata inputMetadata) {
		return getInputsInfo(inputMetadata.inputs());
	}
	
	public static InputInfo[] getInputsInfo(Input[] inputs) {
		return map(Arrays.asList(inputs), input ->
			new InputInfo(input)
			).toArray(new InputInfo[0]);
	}
	
	protected static InputActionInfo[] getActionInfos(Action[] actions) {
		return map(Arrays.asList(actions), action -> new InputActionInfo(action.actionDescriptor(), action.label())).toArray(new InputActionInfo[0]);
	}
	
	
	private MetadataUtils() { }
	
}
