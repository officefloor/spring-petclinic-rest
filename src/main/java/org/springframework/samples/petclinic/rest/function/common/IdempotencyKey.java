package org.springframework.samples.petclinic.rest.function.common;

/**
 * The {@code Idempotency-Key} header value carried down a create pipeline as a variable, so the
 * step that records the created id (see {@link IdempotencyStore}) knows the key without re-reading
 * the request. The wrapper is always published (even when no header was sent) so the recording
 * step's {@code @Val} resolves; {@link #value()} is {@code null} when the request carried no key.
 */
public record IdempotencyKey(String value) {
}
