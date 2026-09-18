/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.rest.controller;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validates an {@link OwnerFieldsDto} as part of the {@code @Valid} pass on the owner
 * request body: required text fields must be present and non-blank, the telephone is
 * normalized to its bare digits and required to be exactly {@link TelephoneNormalizer#REQUIRED_DIGITS}
 * digits long, and an optional email is required to be syntactically valid and normalized to
 * lower case.
 * <p>
 * Registered on the owner controller through {@code @InitBinder} so every rejected field is
 * recorded as a field error on the shared {@link Errors}, surfacing through the usual
 * bad-request handling. A valid telephone is written back to the DTO in normalized form so it
 * is stored and returned as its 10 digits.
 *
 * @author Vitaliy Fedoriv
 */
@Component
public class OwnerFieldsValidator implements Validator {

    private static final String[] REQUIRED_FIELDS = {"firstName", "lastName", "address", "city", "telephone"};

    private final TelephoneNormalizer telephoneNormalizer;

    private final EmailNormalizer emailNormalizer;

    public OwnerFieldsValidator(TelephoneNormalizer telephoneNormalizer, EmailNormalizer emailNormalizer) {
        this.telephoneNormalizer = telephoneNormalizer;
        this.emailNormalizer = emailNormalizer;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return OwnerFieldsDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        OwnerFieldsDto owner = (OwnerFieldsDto) target;
        for (String field : REQUIRED_FIELDS) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, field, "required", "must not be blank");
        }
        if (!errors.hasFieldErrors("telephone")) {
            normalizeTelephone(owner, errors);
        }
        normalizeEmail(owner, errors);
    }

    private void normalizeTelephone(OwnerFieldsDto owner, Errors errors) {
        String digits = telephoneNormalizer.normalize(owner.getTelephone());
        if (telephoneNormalizer.hasRequiredLength(digits)) {
            owner.setTelephone(digits);
        } else {
            errors.rejectValue("telephone", "telephone.invalid",
                "must contain exactly " + TelephoneNormalizer.REQUIRED_DIGITS + " digits");
        }
    }

    private void normalizeEmail(OwnerFieldsDto owner, Errors errors) {
        String email = owner.getEmail();
        if (email == null) {
            return;
        }
        if (emailNormalizer.isValid(email)) {
            owner.setEmail(emailNormalizer.normalize(email));
        } else {
            errors.rejectValue("email", "email.invalid", "must be a valid email address");
        }
    }
}
