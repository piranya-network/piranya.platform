package network.piranya.platform.api.extension_models.execution.liquidity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface LiquidityProviderMetadata {
	String displayName();
	LiquidityProviderCategory category();
	String description() default "";
	String[] features() default {};
	String[] searchTags() default {};
}
