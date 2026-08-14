package org.springframework.samples.petclinic.rest.function.owner;

/**
 * The {@code Idempotency-Key} header value for a {@code POST /api/owners} request, published by
 * {@link CheckIdempotencyKey} and read back by {@link RecordIdempotencyKey}. Wrapping the value in a
 * dedicated type (rather than passing a bare {@code String}) keeps the pipeline variable unambiguous.
 * The value is absent ({@code null}/blank) when the request carried no key.
 */
public final class IdempotencyKey {

    private final String value;

    public IdempotencyKey(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }

    public boolean isPresent() {
        return this.value != null && !this.value.isBlank();
    }
}
