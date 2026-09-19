/*
 * Copyright 2002-2013 the original author or authors.
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

package org.springframework.samples.petclinic.util;

/**
 * Composes an owner's single stored address from the structured address form, preferring the
 * structured fields over the flat address for backward compatibility. When {@code addressLine1}
 * is supplied the composed value is the normalized {@code addressLine1}, followed by a single
 * space and the normalized {@code addressLine2} when that second line is present; otherwise it
 * falls back to the normalized flat address. Each part is canonicalized via
 * {@link AddressNormalizer}, so the composed value is already in canonical form.
 */
public final class AddressComposer {

    private AddressComposer() {
    }

    /**
     * Compose the effective, normalized address from the structured and flat inputs.
     *
     * @param addressLine1 the structured first line, may be {@code null} or blank
     * @param addressLine2 the optional structured second line, may be {@code null} or blank
     * @param flatAddress the flat address, used only when {@code addressLine1} is absent, may be
     * {@code null}
     * @return the normalized {@code addressLine1} (with the normalized {@code addressLine2}
     * appended after a single space when present) when a structured first line is supplied,
     * otherwise the normalized flat address (which may be {@code null})
     */
    public static String compose(String addressLine1, String addressLine2, String flatAddress) {
        String line1 = AddressNormalizer.normalize(addressLine1);
        if (line1 == null || line1.isEmpty()) {
            return AddressNormalizer.normalize(flatAddress);
        }
        String line2 = AddressNormalizer.normalize(addressLine2);
        return (line2 == null || line2.isEmpty()) ? line1 : line1 + " " + line2;
    }
}
