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

package org.springframework.samples.petclinic.rest.advice;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

/**
 * Rejects an owner whose required text fields are missing or blank. Each offending field is
 * reported through the standard validation flow so its name appears in the 400 response.
 */
@ControllerAdvice
public class OwnerFieldsValidationAdvice implements Validator {

    @InitBinder
    void registerOwnerFieldsValidator(WebDataBinder binder) {
        binder.addValidators(this);
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return OwnerFieldsDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        OwnerFieldsDto owner = (OwnerFieldsDto) target;
        rejectIfBlank(errors, "firstName", owner.getFirstName());
        rejectIfBlank(errors, "lastName", owner.getLastName());
        rejectIfBlank(errors, "address", owner.getAddress());
        rejectIfBlank(errors, "city", owner.getCity());
        rejectIfBlank(errors, "telephone", owner.getTelephone());
        normalizeTelephone(errors, owner);
    }

    private void rejectIfBlank(Errors errors, String field, String value) {
        if (value == null || value.isBlank()) {
            errors.rejectValue(field, "required", "must not be blank");
        }
    }

    /**
     * Normalizes the telephone to E.164 form, storing it back on the payload so it is persisted
     * and echoed as-is; rejects the field when no valid E.164 number can be formed.
     */
    private void normalizeTelephone(Errors errors, OwnerFieldsDto owner) {
        if (owner.getTelephone() == null) {
            return;
        }
        String e164 = E164Telephone.toE164(owner.getTelephone());
        if (e164 != null) {
            owner.setTelephone(e164);
        } else {
            errors.rejectValue("telephone", "telephone.invalid", "must be a valid E.164 telephone");
        }
    }
}
