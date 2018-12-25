package network.piranya.platform.api.extension_models;

import java.lang.annotation.Target;

@Target(value = { })
public @interface Action {
	
	String actionDescriptor();
	String label();
	
}
