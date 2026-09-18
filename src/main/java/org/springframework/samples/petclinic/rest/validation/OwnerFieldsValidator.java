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
import java.util.List;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Validates that an owner supplies every mandatory field. A field is considered
 * missing when it is {@code null} or contains only whitespace. The address is
 * satisfied by either the structured {@code addressLine1} or the flat {@code address}.
 */
public final class OwnerFieldsValidator {

    private OwnerFieldsValidator() {
    }

    /**
     * Returns the names of every mandatory field that is missing or blank, in declaration order.
     * An owner must supply an address in either form (a non-blank {@code addressLine1} or the flat
     * {@code address}); when neither is present {@code address} is reported missing.
     *
     * @param ownerFields the submitted owner fields
     * @return the names of the missing fields, or an empty list when all are present
     */
    public static List<String> findMissingFields(OwnerFieldsDto ownerFields) {
        List<String> missing = new ArrayList<>();
        if (isBlank(ownerFields.getFirstName())) {
            missing.add("firstName");
        }
        if (isBlank(ownerFields.getLastName())) {
            missing.add("lastName");
        }
        if (isBlank(ownerFields.getAddressLine1()) && isBlank(ownerFields.getAddress())) {
            missing.add("address");
        }
        if (isBlank(ownerFields.getCity())) {
            missing.add("city");
        }
        if (isBlank(ownerFields.getTelephone())) {
            missing.add("telephone");
        }
        return missing;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
