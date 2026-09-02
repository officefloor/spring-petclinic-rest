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

import java.util.Map;

/**
 * Rejects an owner whose 4-digit postcode is out of range for its city's region: NSW 2000-2099,
 * VIC 3000-3099, QLD 4000-4099. A city with no known region accepts any 4-digit postcode, and an
 * owner with no postcode is left untouched (the field is optional). Non-numeric or wrong-length
 * postcodes are rejected earlier by the field's own pattern constraint.
 */
@ControllerAdvice
public class OwnerPostcodeValidationAdvice implements Validator {

    /** City -> region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> CITY_RANGE = Map.of(
        "Sydney", new int[] {2000, 2099},
        "Melbourne", new int[] {3000, 3099},
        "Brisbane", new int[] {4000, 4099});

    @InitBinder
    void registerOwnerPostcodeValidator(WebDataBinder binder) {
        binder.addValidators(this);
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return OwnerFieldsDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        OwnerFieldsDto owner = (OwnerFieldsDto) target;
        String postcode = owner.getPostcode();
        String city = owner.getCity();
        int[] range = city == null ? null : CITY_RANGE.get(city);
        if (postcode == null || range == null || !postcode.matches("[0-9]{4}")) {
            return;
        }
        int value = Integer.parseInt(postcode);
        if (value < range[0] || value > range[1]) {
            errors.rejectValue("postcode", "postcode.invalid", "must be valid for the owner's city");
        }
    }
}
