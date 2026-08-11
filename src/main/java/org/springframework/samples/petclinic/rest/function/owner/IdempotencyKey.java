package org.springframework.samples.petclinic.rest.function.owner;

/**
 * The create request's {@code Idempotency-Key} header value, published as a variable by
 * {@link CheckIdempotencyKey} so {@link RecordIdempotencyKey} can record the created owner under it.
 *
 * <p>A distinct type (rather than a bare {@code String}) so it is matched unambiguously as a pipeline
 * variable. {@code value} is {@code null} when the request carried no key.
 */
public record IdempotencyKey(String value) {

    /** Whether an actual, non-blank key was supplied. */
    public boolean isPresent() {
        return this.value != null && !this.value.isBlank();
    }
}
