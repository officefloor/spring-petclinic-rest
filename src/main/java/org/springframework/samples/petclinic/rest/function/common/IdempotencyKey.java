package org.springframework.samples.petclinic.rest.function.common;

/**
 * The request's {@code Idempotency-Key} header, published by the first create step and read by
 * the recording step. Wraps the raw value (possibly absent) in a dedicated type so it moves
 * between steps as a distinct variable rather than a bare {@code String}.
 */
public final class IdempotencyKey {

    private final String value;

    public IdempotencyKey(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }

    /** True when the request actually carried a (non-blank) key. */
    public boolean isPresent() {
        return this.value != null && !this.value.isBlank();
    }
}
