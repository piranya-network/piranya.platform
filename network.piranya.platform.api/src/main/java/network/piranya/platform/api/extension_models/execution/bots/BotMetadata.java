package network.piranya.platform.api.extension_models.execution.bots;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface BotMetadata {
	String displayName();
	String[] features() default {};
	boolean singleton() default false;
	String description() default "";
	//Class<BotMetadataDescription> metadataClass
}
