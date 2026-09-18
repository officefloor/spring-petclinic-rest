package org.springframework.samples.petclinic.rest.function.owner;

/**
 * The request's {@code Idempotency-Key} header, published by {@link CheckIdempotencyKey} so a
 * later step can pin it to the created owner. A wrapper (rather than a bare {@code String})
 * gives the variable a distinct type and lets a request without the header flow through as an
 * {@link #absent()} value rather than a null variable.
 *
 * @param value the header value, or {@code null} when the request carried no key.
 */
public record IdempotencyKey(String value) {

    public static IdempotencyKey absent() {
        return new IdempotencyKey(null);
    }

    public boolean isPresent() {
        return this.value != null;
    }
}
