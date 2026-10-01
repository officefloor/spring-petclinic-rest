package org.springframework.samples.petclinic.rest.function.owner;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import net.officefloor.plugin.clazz.Qualifier;

/**
 * Qualifies the per-city existing-owner count published by {@link CheckOwnerCityCapacity} and read by
 * {@link CheckOwnerCityCapacityWarning}, so the single scan the hard-limit check already performs is
 * reused for the approaching-capacity warning rather than recomputed.
 */
@Documented
@Retention(RUNTIME)
@Target(PARAMETER)
@Qualifier
public @interface CityOwnerCount {
}
