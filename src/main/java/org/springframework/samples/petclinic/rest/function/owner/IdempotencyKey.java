package org.springframework.samples.petclinic.rest.function.owner;

/**
 * The {@code Idempotency-Key} header value carried through the create-owner pipeline as a variable,
 * so the step that records the create can pair the key with the new owner's id. The value is
 * {@code null} when the request supplied no such header, in which case the create is not recorded.
 */
public record IdempotencyKey(String value) {
}
