package network.piranya.platform.api.extension_models.execution.bots;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
	String id();
	String displayName() default "";
	String[] features() default {};
	String description() default "";
	int order() default Integer.MAX_VALUE;
}
