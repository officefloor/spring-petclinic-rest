package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TelephoneValidator implements ConstraintValidator<Telephone, String> {

    @Override
    public boolean isValid(String telephone, ConstraintValidatorContext context) {
        String e164 = TelephoneNormalizer.toE164(telephone);
        return e164 != null && E164NationalNumberRule.hasValidNationalLength(e164);
    }
}
