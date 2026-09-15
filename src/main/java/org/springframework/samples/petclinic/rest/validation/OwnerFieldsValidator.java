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
 * Returns the names of any of {@code firstName}, {@code lastName}, an address,
 * {@code city} or {@code telephone} that are missing (null) or blank, in a stable
 * canonical order. An owner supplies an address in either form: a non-blank
 * structured {@code addressLine1} or the flat {@code address}; {@code "address"} is
 * reported missing only when neither is present.
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
        addIfMissingAddress(missing, owner);
        addIfBlank(missing, "city", owner == null ? null : owner.getCity());
        addIfBlank(missing, "telephone", owner == null ? null : owner.getTelephone());
        return missing;
    }

    private void addIfBlank(List<String> missing, String fieldName, String value) {
        if (isBlank(value)) {
            missing.add(fieldName);
        }
    }

    /** An address is present when the structured {@code addressLine1} or the flat
     *  {@code address} is non-blank; otherwise {@code "address"} is reported missing. */
    private void addIfMissingAddress(List<String> missing, OwnerFieldsDto owner) {
        String addressLine1 = owner == null ? null : owner.getAddressLine1();
        String address = owner == null ? null : owner.getAddress();
        if (isBlank(addressLine1) && isBlank(address)) {
            missing.add("address");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
