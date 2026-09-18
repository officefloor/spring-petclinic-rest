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
 * Rejects an {@link OwnerFieldsDto} whose required text fields are missing or blank.
 * <p>
 * Registered on the owner controller through {@code @InitBinder} so it runs as part of the
 * standard {@code @Valid} pass on the request body: every rejected field is recorded as a
 * field error on the shared {@link Errors}, surfacing through the usual bad-request handling.
 *
 * @author Vitaliy Fedoriv
 */
@Component
public class OwnerFieldsValidator implements Validator {

    private static final String[] REQUIRED_FIELDS = {"firstName", "lastName", "address", "city", "telephone"};

    @Override
    public boolean supports(Class<?> clazz) {
        return OwnerFieldsDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        for (String field : REQUIRED_FIELDS) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, field, "required", "must not be blank");
        }
    }
}
