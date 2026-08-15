package org.springframework.samples.petclinic.rest.function.owner;

/**
 * The {@code Idempotency-Key} header value for a create request, published by
 * {@link CheckIdempotencyKey} so that {@link RecordIdempotencyKey} can remember which owner the
 * request created. Carries {@code null} when the request supplied no key.
 */
public record IdempotencyKey(String value) {

    /** Whether a usable (non-blank) key was supplied. */
    public boolean isPresent() {
        return value != null && !value.isBlank();
    }
}
