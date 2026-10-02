package org.springframework.samples.petclinic.rest.function.owner;

/**
 * The request's {@code Idempotency-Key} header, published by {@link CheckIdempotency} on the
 * create path so {@link RecordIdempotency} can associate it with the saved owner. Its own type
 * (rather than a bare {@code String}) keeps it unambiguous as a pipeline variable. The value is
 * {@code null} when the header was absent.
 */
record IdempotencyKey(String value) {

    /** Whether a usable (non-blank) key was supplied. */
    public boolean isPresent() {
        return this.value != null && !this.value.isBlank();
    }
}
