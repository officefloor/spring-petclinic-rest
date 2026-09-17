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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.error.RequiredFieldsMissingException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Validates that an owner submitted to the REST API carries all of its mandatory fields.
 * A field is considered missing when it is {@code null}, empty or blank (whitespace only).
 */
@Component
public class OwnerRequestValidator {

    /**
     * @param owner the submitted owner fields
     * @throws RequiredFieldsMissingException if any required field is missing or blank, naming
     * every offending field
     */
    public void validate(OwnerFieldsDto owner) {
        Map<String, String> requiredFields = new LinkedHashMap<>();
        requiredFields.put("firstName", owner.getFirstName());
        requiredFields.put("lastName", owner.getLastName());
        requiredFields.put("address", owner.getAddress());
        requiredFields.put("city", owner.getCity());
        requiredFields.put("telephone", owner.getTelephone());

        List<String> missingFields = new ArrayList<>();
        requiredFields.forEach((name, value) -> {
            if (!StringUtils.hasText(value)) {
                missingFields.add(name);
            }
        });

        if (!missingFields.isEmpty()) {
            throw new RequiredFieldsMissingException(missingFields);
        }
    }
}
