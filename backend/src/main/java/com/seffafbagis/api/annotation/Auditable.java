package com.seffafbagis.api.annotation;

import com.seffafbagis.api.enums.AuditAction;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for marking methods to be automatically audited.
 * Aspects will intercept methods with this annotation and create audit logs.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    /**
     * The action being performed.
     */
    AuditAction action();

    /**
     * The type of entity being affected.
     * Optional if it can be inferred or is generic.
     */
    String entityType() default "";
}
