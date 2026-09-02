package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Carries the request's {@code Idempotency-Key} (possibly {@code null}) from the first step to the
 * step that records the created owner. A dedicated type so the pipeline variable matches unambiguously.
 */
public record IdempotencyToken(String key) {
}
