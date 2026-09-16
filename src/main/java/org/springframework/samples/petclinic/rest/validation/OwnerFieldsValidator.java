/*
 * Copyright 2016-2017 the original author or authors.
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
import java.util.List;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Validates that an {@link OwnerFieldsDto} carries a non-blank value for every
 * required owner field. This complements the schema-level Bean Validation
 * constraints: {@code address} and {@code city} have no pattern, so a
 * whitespace-only value would otherwise satisfy their {@code @Size(min = 1)}
 * constraint and slip through.
 */
public final class OwnerFieldsValidator {

    private OwnerFieldsValidator() {
    }

    /**
     * Rejects an owner whose required fields are missing or blank.
     *
     * @param ownerFields the submitted owner fields
     * @throws MissingOwnerFieldsException if any of {@code firstName}, {@code lastName},
     *                                     {@code address}, {@code city} or {@code telephone}
     *                                     is {@code null} or blank
     */
    public static void validateRequiredFields(OwnerFieldsDto ownerFields) {
        List<String> missing = new ArrayList<>();
        addIfBlank(missing, "firstName", ownerFields.getFirstName());
        addIfBlank(missing, "lastName", ownerFields.getLastName());
        addIfBlank(missing, "address", ownerFields.getAddress());
        addIfBlank(missing, "city", ownerFields.getCity());
        addIfBlank(missing, "telephone", ownerFields.getTelephone());
        if (!missing.isEmpty()) {
            throw new MissingOwnerFieldsException(missing);
        }
    }

    private static void addIfBlank(List<String> missing, String fieldName, String value) {
        if (value == null || value.isBlank()) {
            missing.add(fieldName);
        }
    }
}
