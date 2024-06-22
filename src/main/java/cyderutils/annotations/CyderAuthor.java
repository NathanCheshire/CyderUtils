package cyderutils.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation used to give credit of functional pieces of Cyder to their respective authors.
 */
@Retention(RetentionPolicy.RUNTIME) /* allow to be found after compilation to bytecode */
@Target(ElementType.TYPE) /* restrict annotations to classes */
public @interface CyderAuthor {
    String author() default "Nate Cheshire";
}
