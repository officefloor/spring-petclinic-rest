package org.springframework.samples.petclinic.rest.function.owner;

/**
 * The create request's {@code Idempotency-Key} header, published as a variable by {@link CheckIdempotency}
 * so {@link RecordIdempotency} can associate the key with the newly created owner after it is saved.
 *
 * <p>A dedicated wrapper (rather than a bare {@code String}) so the variable is matched by a distinct type
 * and never collides with other string state in the pipeline. The wrapped value is {@code null} when the
 * request carried no (or a blank) key, in which case nothing is recorded.
 */
public final class IdempotencyKey {

    private final String value;

    public IdempotencyKey(String value) {
        this.value = value;
    }

    /** The raw header value, or {@code null} when absent/blank. */
    public String value() {
        return value;
    }
}
