package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Carries the request's {@code Idempotency-Key} header between pipeline steps. Always
 * published (so later steps can read it by type), with a {@code null} {@link #value()}
 * when the request supplied no key.
 */
public record IdempotencyKey(String value) {
}
