package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Flow taken by {@link CheckIdempotencyKey} to run the normal owner-create pipeline when the request
 * is not an idempotent repeat. Not calling it short-circuits the pipeline (the idempotent response
 * has already been sent).
 */
@FunctionalInterface
public interface CreateFlow {
    void create();
}
