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

import java.time.LocalDate;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

/**
 * Rejects an owner whose supplied registration date lies in the future (later than the current
 * server date), reporting it through the standard validation flow so the request fails with 400.
 * An owner with no registration date is left untouched (the field is optional and defaults to today).
 */
@ControllerAdvice
public class OwnerRegistrationDateValidationAdvice implements Validator {

    @InitBinder
    void registerOwnerRegistrationDateValidator(WebDataBinder binder) {
        binder.addValidators(this);
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return OwnerFieldsDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        LocalDate registrationDate = ((OwnerFieldsDto) target).getRegistrationDate();
        if (registrationDate != null && registrationDate.isAfter(LocalDate.now())) {
            errors.rejectValue("registrationDate", "registrationDate.future",
                "must not be later than the current date");
        }
    }
}
