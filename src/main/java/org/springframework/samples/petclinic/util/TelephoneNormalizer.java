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
 * Normalizes telephone numbers to their bare digits. Shared by the create-time validation
 * constraint and the mapping that stores the value, so both agree on what "the digits" are.
 */
public final class TelephoneNormalizer {

    private TelephoneNormalizer() {
    }

    /**
     * Strip every non-digit character from the given telephone value.
     *
     * @param telephone the raw telephone value (may be {@code null})
     * @return the digits contained in {@code telephone}, or an empty string when it is {@code null}
     */
    public static String normalize(String telephone) {
        if (telephone == null) {
            return "";
        }
        return telephone.replaceAll("\\D", "");
    }
}
