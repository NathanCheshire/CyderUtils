package cyder.annotations;

import cyder.enumerations.CyderInspection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation used to mark methods of Cyder exempt from certain Cyder-specific reflection checks.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({
        ElementType.METHOD,
        ElementType.CONSTRUCTOR,
        ElementType.FIELD,
        ElementType.TYPE
})
public @interface SuppressCyderInspections {
    CyderInspection[] value();
}
