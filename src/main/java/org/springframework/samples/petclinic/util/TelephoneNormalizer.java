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

package org.springframework.samples.petclinic.util;

import org.springframework.stereotype.Component;

/**
 * Produces the canonical stored form of an owner's telephone number.
 * <p>
 * Centralising this here keeps the REST controllers thin: they simply delegate to
 * {@link #normalize(String)} both when canonicalising an incoming value before it is
 * persisted and when comparing values to detect duplicates, so the two always agree.
 * <p>
 * The current canonical form is the bare digits of the supplied value (every non-digit
 * character is removed). Bean Validation on the request DTO has already guaranteed that a
 * non-null value carries exactly the expected number of digits (rejecting anything else
 * with a 400).
 */
@Component
public class TelephoneNormalizer {

    /**
     * Normalizes the supplied telephone value to its canonical stored form by removing
     * every non-digit character. Values that differ only in formatting (spaces, dashes,
     * parentheses) therefore normalize to the same result.
     *
     * @param telephone the raw telephone value (may be {@code null})
     * @return the normalized telephone, or {@code null} if the input was {@code null}
     */
    public String normalize(String telephone) {
        return telephone == null ? null : telephone.replaceAll("\\D", "");
    }
}
