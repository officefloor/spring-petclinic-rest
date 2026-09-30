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

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

/**
 * Single source of truth for the E.164 international dialling codes the application recognizes and the
 * exact number of national (subscriber) digits each requires. Both the create-time telephone validation
 * (in the owner controller) and the read-time display formatting (in the owner mapper) consult this
 * table, so the recognized set of country codes has one owner: add a code here and both paths pick it up
 * without hand-syncing separate literal lists.
 */
public final class E164PhoneNumbers {

    /**
     * International dialling codes mapped to the exact number of national (subscriber) digits an E.164
     * number for that country must carry: {@code +61} (Australia) requires 9 national digits and
     * {@code +1} (NANP) requires 10. Country codes absent from this map have no configured length rule.
     */
    public static final Map<String, Integer> NATIONAL_LENGTHS = Map.of("61", 9, "1", 10);

    private E164PhoneNumbers() {
    }

    /**
     * Select the recognized dialling code for a run of E.164 digits (the number without its leading
     * {@code '+'}): the longest configured code in {@link #NATIONAL_LENGTHS} that prefixes the digits, or
     * empty when none is configured for the number.
     *
     * @param digits the E.164 digits without the leading {@code '+'}
     * @return the matching dialling code, longest prefix winning, or empty when none matches
     */
    public static Optional<String> countryCode(String digits) {
        return NATIONAL_LENGTHS.keySet().stream()
            .filter(digits::startsWith)
            .max(Comparator.comparingInt(String::length));
    }
}
