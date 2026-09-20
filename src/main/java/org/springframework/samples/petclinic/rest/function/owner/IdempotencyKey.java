package org.springframework.samples.petclinic.rest.function.owner;

/**
 * The request's {@code Idempotency-Key}, published by {@link CheckIdempotencyKey} so
 * {@link RecordIdempotencyKey} can register the created owner under it. The value is
 * {@code null} when the request carried no key.
 */
public record IdempotencyKey(String value) {

    /** Whether an actual key was supplied (present and non-blank). */
    public boolean isPresent() {
        return this.value != null && !this.value.isBlank();
    }
}
