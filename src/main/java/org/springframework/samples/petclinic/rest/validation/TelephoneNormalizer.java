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

package org.springframework.samples.petclinic.rest.validation;

import org.springframework.stereotype.Component;

/**
 * Normalizes and validates an owner's telephone number on create: every
 * non-digit character is stripped and the result is required to be exactly
 * {@link #REQUIRED_DIGITS} digits long.
 */
@Component
public class TelephoneNormalizer {

    /** The exact number of digits a normalized telephone must contain. */
    public static final int REQUIRED_DIGITS = 10;

    /**
     * Remove every non-digit character from the given telephone.
     *
     * @param telephone the raw telephone value (may be {@code null})
     * @return the digits-only telephone, never {@code null}
     */
    public String normalize(String telephone) {
        return telephone == null ? "" : telephone.replaceAll("\\D", "");
    }

    /**
     * @param normalized a value already produced by {@link #normalize(String)}
     * @return {@code true} if it is exactly {@link #REQUIRED_DIGITS} digits long
     */
    public boolean hasRequiredDigits(String normalized) {
        return normalized.length() == REQUIRED_DIGITS;
    }
}
