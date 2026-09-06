package org.springframework.samples.petclinic.rest.validation;

import java.util.List;

import org.springframework.samples.petclinic.rest.advice.InvalidOwnerFieldsException;
import org.springframework.stereotype.Component;

/**
 * Normalizes owner telephone numbers into the single canonical form that is stored, returned and
 * compared for duplicates. Keeping this logic in one place lets the create endpoint treat a
 * telephone as an opaque, already-canonical value: it {@link #normalize(String) normalizes} an
 * incoming number once, then relies on {@link #canonicalize(String)} to compare it against
 * existing owners regardless of how those numbers were originally formatted.
 */
@Component
public class TelephoneNormalizer {

    /**
     * Normalizes a telephone number submitted to the create endpoint by removing every non-digit
     * character. The result must contain exactly 10 digits; otherwise the submission is rejected as
     * a 400 (via {@link InvalidOwnerFieldsException}, which the {@code ExceptionControllerAdvice}
     * renders with {@code telephone} in its {@code errors} array).
     *
     * @param telephone the raw telephone value from the request
     * @return the canonical telephone to store and return
     * @throws InvalidOwnerFieldsException if the value cannot form a valid telephone
     */
    public String normalize(String telephone) {
        String digits = canonicalize(telephone);
        if (digits.length() != 10) {
            throw new InvalidOwnerFieldsException(List.of("telephone"));
        }
        return digits;
    }

    /**
     * Reduces an arbitrary telephone (possibly {@code null} or differently formatted) to the
     * canonical form used to compare two numbers for equality, so numbers that differ only in
     * formatting still collide. Unlike {@link #normalize(String)} this never rejects its input; it
     * is applied to existing owners' stored telephones during duplicate detection.
     *
     * @param telephone a stored or submitted telephone, or {@code null}
     * @return the telephone reduced to its canonical comparison form
     */
    public String canonicalize(String telephone) {
        return telephone == null ? "" : telephone.replaceAll("\\D", "");
    }
}
