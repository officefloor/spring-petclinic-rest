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

import org.springframework.samples.petclinic.model.RegionPostcodes;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Enforces that a supplied owner postcode is valid for the city's region.
 * <p>
 * Validation runs only when a postcode is present and well-formed (4 digits); its
 * shape is enforced declaratively by the DTO's {@code @Pattern} constraint. Registered
 * on the owner request binder so an out-of-range postcode surfaces through the same
 * {@code BindingResult} as the other owner-field checks, yielding a 400 response.
 */
@Component
public class PostcodeValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return OwnerFieldsDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        OwnerFieldsDto owner = (OwnerFieldsDto) target;
        String postcode = owner.getPostcode();
        if (postcode == null || !postcode.matches("[0-9]{4}")) {
            return;
        }
        if (!RegionPostcodes.isValidForCity(owner.getCity(), Integer.parseInt(postcode))) {
            errors.rejectValue("postcode", "postcode.outOfRange",
                "postcode is not valid for the city's region");
        }
    }
}
