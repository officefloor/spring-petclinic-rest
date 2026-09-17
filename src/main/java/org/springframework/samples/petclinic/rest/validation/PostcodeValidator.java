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

package org.springframework.samples.petclinic.rest.validation;

import java.util.regex.Pattern;

import org.springframework.samples.petclinic.model.Regions;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.error.InvalidPostcodeException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Validates an owner's optional postcode. When present it must be a 4-digit code and, if the
 * owner's city maps to a known region, must fall within that region's inclusive postcode range.
 * A city with no known region accepts any 4-digit postcode. An absent postcode is always accepted.
 */
@Component
public class PostcodeValidator {

    private static final Pattern FOUR_DIGITS = Pattern.compile("\\d{4}");

    /**
     * @param owner the submitted owner fields
     * @throws InvalidPostcodeException if a postcode is present but is not 4 digits or is out of
     * range for the region of the owner's city
     */
    public void validate(OwnerFieldsDto owner) {
        String postcode = owner.getPostcode();
        if (!StringUtils.hasText(postcode)) {
            return;
        }
        if (!FOUR_DIGITS.matcher(postcode).matches()) {
            throw new InvalidPostcodeException(postcode);
        }
        int[] range = Regions.rangeOf(Regions.regionOfCity(owner.getCity()));
        if (range != null) {
            int value = Integer.parseInt(postcode);
            if (value < range[0] || value > range[1]) {
                throw new InvalidPostcodeException(postcode);
            }
        }
    }
}
