package org.springframework.samples.petclinic.rest.function.owner;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import net.officefloor.plugin.clazz.Qualifier;

/**
 * Qualifies the composite {@code riskFlag} boolean variable so it does not collide with the other
 * boolean variables ({@code bulkSignupWarning}, {@code capacityWarning}) flowing through the same
 * pipeline.
 *
 * @see ResolveRiskFlag
 */
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface RiskFlag {
}
