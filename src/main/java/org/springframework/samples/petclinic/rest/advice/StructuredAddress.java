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

/**
 * Applies the structured address form to an owner payload. Normalizes 'addressLine1', 'addressLine2'
 * and the flat 'address' with {@link AddressNormalizer}, then composes the flat 'address' from the
 * structured fields when an 'addressLine1' is supplied (the normalized line 1, plus a single space
 * and the normalized line 2 when present), and otherwise keeps the normalized flat 'address'. The
 * composed value is written back to the payload and returned so the caller can validate its presence.
 */
final class StructuredAddress {

    private StructuredAddress() {
    }

    static String apply(OwnerFieldsDto owner) {
        String line1 = AddressNormalizer.normalize(owner.getAddressLine1());
        String line2 = AddressNormalizer.normalize(owner.getAddressLine2());
        owner.setAddressLine1(line1);
        owner.setAddressLine2(line2);
        String address;
        if (line1 == null || line1.isBlank()) {
            address = AddressNormalizer.normalize(owner.getAddress());
        } else {
            address = line2 == null || line2.isBlank() ? line1 : line1 + " " + line2;
        }
        owner.setAddress(address);
        return address;
    }
}
