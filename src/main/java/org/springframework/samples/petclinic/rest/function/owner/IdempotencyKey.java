package org.springframework.samples.petclinic.rest.function.owner;

/**
 * The create request's {@code Idempotency-Key} header, carried between pipeline steps as a
 * variable. Holds {@code null} when the header is absent or blank, so downstream steps can
 * decide whether idempotent replay/recording applies.
 */
public record IdempotencyKey(String value) {

    /** Whether a usable (non-blank) key was supplied. */
    public boolean isPresent() {
        return value != null && !value.isBlank();
    }
}
