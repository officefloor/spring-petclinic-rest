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
import java.util.function.Function;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Validates that an owner supplies every mandatory field. A field is considered
 * missing when it is {@code null} or contains only whitespace.
 */
public final class OwnerFieldsValidator {

    /** The mandatory owner fields, mapped from their JSON name to their accessor. */
    private static final Map<String, Function<OwnerFieldsDto, String>> REQUIRED_FIELDS = new LinkedHashMap<>();

    static {
        REQUIRED_FIELDS.put("firstName", OwnerFieldsDto::getFirstName);
        REQUIRED_FIELDS.put("lastName", OwnerFieldsDto::getLastName);
        REQUIRED_FIELDS.put("address", OwnerFieldsDto::getAddress);
        REQUIRED_FIELDS.put("city", OwnerFieldsDto::getCity);
        REQUIRED_FIELDS.put("telephone", OwnerFieldsDto::getTelephone);
    }

    private OwnerFieldsValidator() {
    }

    /**
     * Returns the names of every mandatory field that is missing or blank, in declaration order.
     *
     * @param ownerFields the submitted owner fields
     * @return the names of the missing fields, or an empty list when all are present
     */
    public static List<String> findMissingFields(OwnerFieldsDto ownerFields) {
        List<String> missing = new ArrayList<>();
        REQUIRED_FIELDS.forEach((name, accessor) -> {
            String value = accessor.apply(ownerFields);
            if (value == null || value.isBlank()) {
                missing.add(name);
            }
        });
        return missing;
    }
}
