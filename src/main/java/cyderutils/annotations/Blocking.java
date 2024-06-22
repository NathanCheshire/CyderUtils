package cyderutils.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.temporal.ChronoUnit;

/**
 * An annotation used to indicate the method is blocking meaning invocation of it
 * will block the calling {@link Thread}. If this is not intended, surround the invocation
 * in a new thread. In Cyder usage of {@link cyderutils.threads.CyderThreadRunner} is common.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Blocking {
    long amount() default 0;
    ChronoUnit unit() default ChronoUnit.SECONDS;
}
