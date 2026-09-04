/*
 * Copyright 2016-2017 the original author or authors.
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

import org.springframework.stereotype.Component;

/**
 * Normalises a raw telephone number to the single canonical form under which it is both
 * stored and compared for duplicates.
 *
 * <p>Keeping this in one place means the stored form and the form used to detect duplicate
 * telephones can never drift apart: an owner is a duplicate of another exactly when their
 * telephones normalise to the same value.
 *
 * <p>The current canonical form is the ten-digit national number with every non-digit
 * character removed; anything that does not reduce to ten digits has no canonical form.
 */
@Component
public class TelephoneNormalizer {

    /**
     * Reduce a raw telephone to its canonical stored form.
     *
     * @param rawTelephone the telephone as supplied by the client, possibly {@code null} or
     *                     containing spaces, dashes or brackets
     * @return the canonical telephone, or {@code null} if {@code rawTelephone} cannot form a
     *         valid telephone
     */
    public String normalize(String rawTelephone) {
        String digits = rawTelephone == null ? "" : rawTelephone.replaceAll("\\D", "");
        return digits.length() == 10 ? digits : null;
    }
}
