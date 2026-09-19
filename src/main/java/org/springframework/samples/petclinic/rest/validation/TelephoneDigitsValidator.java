package org.springframework.samples.petclinic.rest.validation;

import org.springframework.samples.petclinic.util.TelephoneNormalizer;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TelephoneDigitsValidator implements ConstraintValidator<TelephoneDigits, String> {

    @Override
    public boolean isValid(String telephone, ConstraintValidatorContext context) {
        if (telephone == null) {
            return true;
        }
        return TelephoneNormalizer.toE164(telephone).isPresent();
    }
}
