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

import java.util.ArrayList;
import java.util.List;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.stereotype.Component;

/**
 * Verifies that the required identifying fields of an owner request are present and
 * non-blank.
 *
 * <p>Bean Validation on {@link OwnerFieldsDto} already rejects {@code null} values and
 * values failing a field's format, but a purely whitespace value can still slip through
 * for free-text fields such as address and city. This validator closes that gap by
 * treating any missing or blank value as invalid.
 */
@Component
public class OwnerFieldsValidator {

    /**
     * <p>An owner supplies an address in either form: the structured {@code addressLine1} or the
     * flat {@code address}. It is valid when at least one of them is non-blank; when neither is,
     * {@code addressLine1} is reported as missing.
     *
     * @param fields the submitted owner fields
     * @throws MissingOwnerFieldsException if any required field is missing or blank
     */
    public void validate(OwnerFieldsDto fields) {
        List<String> missing = new ArrayList<>();
        checkRequired("firstName", fields.getFirstName(), missing);
        checkRequired("lastName", fields.getLastName(), missing);
        if (isBlank(fields.getAddressLine1()) && isBlank(fields.getAddress())) {
            missing.add("addressLine1");
        }
        checkRequired("city", fields.getCity(), missing);
        checkRequired("telephone", fields.getTelephone(), missing);
        if (!missing.isEmpty()) {
            throw new MissingOwnerFieldsException(missing);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void checkRequired(String name, String value, List<String> missing) {
        if (isBlank(value)) {
            missing.add(name);
        }
    }
}
