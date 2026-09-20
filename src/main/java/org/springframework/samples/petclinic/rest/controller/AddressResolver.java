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

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

/**
 * Reconciles an owner's two accepted address forms into the canonical fields stored on the
 * {@link Owner}. The structured form ({@code addressLine1} plus optional {@code addressLine2}) is
 * preferred whenever a non-blank {@code addressLine1} is supplied; otherwise the flat
 * {@code address} input is used, keeping earlier minimal payloads accepted.
 * <p>
 * Each supplied address field is canonicalized with the shared {@link AddressNormalizer}, and the
 * composed {@code address} is set to the normalized {@code addressLine1} with a single space and
 * the normalized {@code addressLine2} appended when an {@code addressLine2} is present. The
 * normalized structured lines are written back so everything that later reads the address sees the
 * structured values when present and the flat address otherwise.
 */
@Component
public class AddressResolver {

    private final AddressNormalizer addressNormalizer;

    public AddressResolver(AddressNormalizer addressNormalizer) {
        this.addressNormalizer = addressNormalizer;
    }

    /**
     * Whether {@code addressLine1} carries a usable structured address once normalized.
     */
    public boolean hasStructuredAddress(String addressLine1) {
        return !addressNormalizer.normalize(addressLine1).isEmpty();
    }

    /**
     * Whether the flat {@code address} input carries a usable address once normalized.
     */
    public boolean hasFlatAddress(String address) {
        return !addressNormalizer.normalize(address).isEmpty();
    }

    /**
     * Normalizes {@code owner}'s address fields in place and sets its composed {@code address}.
     * When a structured {@code addressLine1} is present it is preferred; otherwise the flat
     * {@code address} is normalized and the structured lines are cleared.
     */
    public void resolve(Owner owner) {
        String line1 = addressNormalizer.normalize(owner.getAddressLine1());
        if (line1.isEmpty()) {
            owner.setAddressLine1(null);
            owner.setAddressLine2(null);
            owner.setAddress(addressNormalizer.normalize(owner.getAddress()));
            return;
        }
        String line2 = addressNormalizer.normalize(owner.getAddressLine2());
        owner.setAddressLine1(line1);
        owner.setAddressLine2(line2.isEmpty() ? null : line2);
        owner.setAddress(line2.isEmpty() ? line1 : line1 + " " + line2);
    }
}
