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
import org.springframework.util.StringUtils;

/**
 * Reconciles the two accepted address forms into a single canonical, normalized address before an
 * owner is validated and stored. The structured form ({@code addressLine1} with an optional
 * {@code addressLine2}) is preferred when present; otherwise the flat {@code address} is kept for
 * backward compatibility. Whichever fields are supplied are normalized with the shared
 * {@link AddressNormalizer}, and the flat {@code address} is (re)composed as the normalized first
 * line with the normalized second line appended after a single space when a second line is present.
 * Every later consumer therefore reads one normalized, composed address.
 */
@Component
public class AddressFormNormalizer {

    private final AddressNormalizer addressNormalizer;

    public AddressFormNormalizer(AddressNormalizer addressNormalizer) {
        this.addressNormalizer = addressNormalizer;
    }

    /**
     * Normalize the supplied address fields in place and compose the flat {@code address}.
     *
     * @param owner the submitted owner fields, mutated so its address fields hold their normalized,
     * composed values
     */
    public void normalize(OwnerFieldsDto owner) {
        String line1 = this.addressNormalizer.normalize(owner.getAddressLine1());
        if (StringUtils.hasText(line1)) {
            String line2 = this.addressNormalizer.normalize(owner.getAddressLine2());
            boolean hasLine2 = StringUtils.hasText(line2);
            owner.setAddressLine1(line1);
            owner.setAddressLine2(hasLine2 ? line2 : null);
            owner.setAddress(hasLine2 ? line1 + " " + line2 : line1);
        }
        else {
            owner.setAddress(this.addressNormalizer.normalize(owner.getAddress()));
        }
    }
}
