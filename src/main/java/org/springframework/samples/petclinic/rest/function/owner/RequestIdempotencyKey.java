package org.springframework.samples.petclinic.rest.function.owner;

/**
 * The create request's {@code Idempotency-Key} header value, published by
 * {@link CheckIdempotencyKey} so {@link AssignOwnerIdempotencyKey} can stamp it onto the new
 * owner. A distinct type (rather than a bare {@code String}) keeps it unambiguous as a pipeline
 * variable and carries {@code null} when the header was absent.
 */
final class RequestIdempotencyKey {

    private final String value;

    RequestIdempotencyKey(String value) {
        this.value = value;
    }

    /** The header value, or {@code null} when the request carried no key. */
    String value() {
        return this.value;
    }
}
