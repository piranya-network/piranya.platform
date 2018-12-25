package network.piranya.platform.api.extension_models;

import java.lang.annotation.Target;

@Target(value = { })
public @interface Input {
	
	String id();
	String displayName();
	InputType type();
	String defaultValue() default "";
	int minEntries() default 1;
	int maxEntries() default 1;
	String constraints() default "";
	String description() default "";
	boolean savable() default false;
	boolean saveByDefault() default false;
	Action[] actions() default {};
	
}
