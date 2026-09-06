package org.springframework.samples.petclinic.rest.validation;

import java.util.Locale;

import org.springframework.stereotype.Component;

/**
 * Normalizes owner email addresses into the single canonical form that is stored, returned and
 * compared when an owner is created or updated. Keeping this logic in one place lets the create and
 * update endpoints treat an email as an opaque, already-canonical value: they
 * {@link #normalize(String) normalize} an incoming email once, then rely on that same form both for
 * what is persisted and for comparing against existing owners.
 *
 * <p>Normalization lower-cases the value (using {@link Locale#ROOT} so it is locale-independent). A
 * {@code null} email is left as-is; syntactic validity is enforced separately by Bean Validation
 * ({@code @Email} on the owner fields), so an invalid address is rejected as a 400 before this runs.
 */
@Component
public class EmailNormalizer {

    /**
     * Normalizes an owner email: when present it is lower-cased (using {@link Locale#ROOT} so
     * normalization is locale-independent); a missing ({@code null}) email is left as-is.
     *
     * @param email the raw email value from the request, or {@code null} when omitted
     * @return the lower-cased email, or {@code null} when none was supplied
     */
    public String normalize(String email) {
        return email == null ? null : email.toLowerCase(Locale.ROOT);
    }
}
