package org.springframework.samples.petclinic.rest.validation;

import java.util.Map;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.springframework.samples.petclinic.model.PhoneNumbers;

/**
 * Checks the national-number length of a telephone once normalised to E.164, per country code.
 */
public class E164NationalLengthValidator implements ConstraintValidator<E164NationalLength, String> {

    /** Country code (with leading '+') to the number of national digits it requires. */
    private static final Map<String, Integer> NATIONAL_DIGITS = Map.of("+61", 9, "+1", 10);

    @Override
    public boolean isValid(String raw, ConstraintValidatorContext context) {
        if (raw == null || raw.isBlank()) {
            return true;
        }
        String e164 = PhoneNumbers.toE164(raw);
        for (Map.Entry<String, Integer> rule : NATIONAL_DIGITS.entrySet()) {
            if (e164.startsWith(rule.getKey())) {
                return e164.length() - rule.getKey().length() == rule.getValue();
            }
        }
        return true;
    }
}
