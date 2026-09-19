package org.springframework.samples.petclinic.rest.function.owner;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import net.officefloor.plugin.clazz.Qualifier;

/**
 * Qualifies the per-city {@code capacityWarning} boolean variable so it does not collide with the
 * unqualified per-day {@code bulkSignupWarning} boolean flowing through the same pipeline.
 *
 * @see ResolveCapacityWarning
 */
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface CapacityWarning {
}
