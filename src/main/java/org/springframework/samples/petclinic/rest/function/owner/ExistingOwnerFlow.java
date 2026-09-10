package org.springframework.samples.petclinic.rest.function.owner;

/**
 * {@code @Flow} branch taken when an {@code Idempotency-Key} has already created an owner:
 * routes to the step that responds 200 with that originally created owner instead of
 * continuing on to create a duplicate.
 */
@FunctionalInterface
public interface ExistingOwnerFlow {
    void flow();
}
