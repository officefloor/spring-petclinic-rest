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

import java.util.List;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Rejects an {@link OwnerFieldsDto} whose required identity fields are missing or blank
 * (null, empty or whitespace-only). This complements the schema's bean-validation
 * constraints, which do not treat a whitespace-only address or city as invalid.
 */
public class OwnerFieldsValidator implements Validator {

    private static final List<String> REQUIRED_FIELDS =
        List.of("firstName", "lastName", "address", "city", "telephone");

    @Override
    public boolean supports(Class<?> clazz) {
        return OwnerFieldsDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        for (String field : REQUIRED_FIELDS) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, field, "required", "must not be blank");
        }
        OwnerFieldsDto owner = (OwnerFieldsDto) target;
        String telephone = owner.getTelephone();
        String digits = telephone == null ? "" : telephone.replaceAll("\\D", "");
        if (digits.length() == 10) {
            owner.setTelephone(digits);
        } else {
            errors.rejectValue("telephone", "telephone", "must be exactly 10 digits");
        }
    }
}
