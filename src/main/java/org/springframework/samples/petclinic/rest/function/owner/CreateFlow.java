package org.springframework.samples.petclinic.rest.function.owner;

/**
 * The {@code create} branch out of {@link CheckIdempotency}: run the normal owner-creation pipeline.
 * Taken when the request carries no idempotency key, or a key that has not been seen before.
 */
@FunctionalInterface
public interface CreateFlow {
    void create();
}
