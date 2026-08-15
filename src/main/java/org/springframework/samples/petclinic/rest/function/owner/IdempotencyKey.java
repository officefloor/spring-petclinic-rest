package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Pipeline variable holding the create request's {@code Idempotency-Key} header value (or
 * {@code null} when the header is absent). Published by {@link CheckIdempotencyKey} and read by
 * {@link RecordIdempotencyKey} so the key can be recorded against the created owner only once the
 * create succeeds.
 */
public record IdempotencyKey(String value) {
}
