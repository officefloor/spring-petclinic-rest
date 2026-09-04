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

import java.util.Optional;

/**
 * Normalization and comparison of owner telephone numbers. Kept separate from the owner controller
 * and the {@code Owner} model so the rules for shaping and comparing a telephone live in one place,
 * as pure functions with no web or persistence dependencies.
 *
 * <p>A submitted telephone is reduced to the canonical form that is stored and returned by removing
 * every non-digit character; the result is accepted only when it is exactly ten digits. Two
 * telephones denote the same number when their {@linkplain #toComparisonKey(String) comparison keys}
 * are equal.
 */
public abstract class TelephoneNormalizer {

    /**
     * Reduce a submitted telephone to the canonical form to be stored and returned: every non-digit
     * character is removed, and the result is accepted only when exactly ten digits remain.
     *
     * @param rawTelephone the submitted telephone (non-null)
     * @return the canonical telephone, or {@link Optional#empty()} if {@code rawTelephone} does not
     *         reduce to a valid telephone
     */
    public static Optional<String> normalize(String rawTelephone) {
        String digits = rawTelephone.replaceAll("\\D", "");
        if (digits.length() != 10) {
            return Optional.empty();
        }
        return Optional.of(digits);
    }

    /**
     * The key used to decide whether two telephones denote the same number. Every non-digit
     * character is stripped so that telephones stored in different textual shapes still compare
     * equal on their digits alone.
     *
     * @param telephone a stored or already-normalized telephone (non-null)
     * @return the comparison key
     */
    public static String toComparisonKey(String telephone) {
        return telephone.replaceAll("\\D", "");
    }

}
