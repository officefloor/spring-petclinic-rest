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
import org.springframework.stereotype.Component;

/**
 * Authoritative check for the owner fields that must be present and non-blank.
 * <p>
 * Returns the names of any of {@code firstName}, {@code lastName}, {@code address},
 * {@code city} or {@code telephone} that are missing (null) or blank, in a stable
 * canonical order.
 */
@Component
public class OwnerFieldsValidator {

    /**
     * @param owner the submitted owner fields (may be {@code null})
     * @return the names of the required fields that are missing or blank, in canonical order;
     *         empty when every required field is present and non-blank
     */
    public List<String> findMissingOrBlankFields(OwnerFieldsDto owner) {
        List<String> missing = new ArrayList<>();
        addIfBlank(missing, "firstName", owner == null ? null : owner.getFirstName());
        addIfBlank(missing, "lastName", owner == null ? null : owner.getLastName());
        addIfBlank(missing, "address", owner == null ? null : owner.getAddress());
        addIfBlank(missing, "city", owner == null ? null : owner.getCity());
        addIfBlank(missing, "telephone", owner == null ? null : owner.getTelephone());
        return missing;
    }

    private void addIfBlank(List<String> missing, String fieldName, String value) {
        if (value == null || value.isBlank()) {
            missing.add(fieldName);
        }
    }
}
