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

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.stereotype.Component;

/**
 * Reconciles the structured and flat address inputs of an owner into their canonical stored
 * form. Each supplied address field is normalized by {@link AddressNormalizer}, and the flat
 * {@code address} is set to the effective address: the composed structured address (the
 * normalized {@code addressLine1}, with a single space and the normalized {@code addressLine2}
 * appended when present) when a structured {@code addressLine1} is supplied, otherwise the
 * normalized flat {@code address}. This keeps the flat {@code address} the single value every
 * downstream reader sees, while the structured fields remain available in their normalized form.
 */
@Component
public class AddressResolver {

    private final AddressNormalizer addressNormalizer;

    public AddressResolver(AddressNormalizer addressNormalizer) {
        this.addressNormalizer = addressNormalizer;
    }

    /**
     * Normalizes the address fields of {@code owner} in place and sets its flat {@code address}
     * to the effective address, preferring the structured fields when {@code addressLine1} is
     * present. A {@code null} owner is left untouched.
     */
    public void resolve(OwnerFieldsDto owner) {
        if (owner == null) {
            return;
        }
        String line1 = addressNormalizer.normalize(owner.getAddressLine1());
        String line2 = addressNormalizer.normalize(owner.getAddressLine2());
        String flat = addressNormalizer.normalize(owner.getAddress());
        owner.setAddressLine1(line1);
        owner.setAddressLine2(line2);
        owner.setAddress(isPresent(line1) ? compose(line1, line2) : flat);
    }

    /** Compose the normalized structured lines: {@code addressLine1} plus a space and
     *  {@code addressLine2} when {@code addressLine2} is present. */
    private String compose(String line1, String line2) {
        return isPresent(line2) ? line1 + " " + line2 : line1;
    }

    private boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }
}
