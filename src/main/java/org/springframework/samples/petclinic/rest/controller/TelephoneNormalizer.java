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

import org.springframework.stereotype.Component;

/**
 * Reduces an owner telephone number to its bare digits.
 * <p>
 * A canonical telephone is exactly {@link #REQUIRED_DIGITS} digits with every separator,
 * prefix or other non-digit character removed, so numbers that differ only in formatting
 * are stored and compared identically.
 */
@Component
public class TelephoneNormalizer {

    /** The exact number of digits a normalized telephone must contain. */
    public static final int REQUIRED_DIGITS = 10;

    /**
     * Strips every non-digit character from {@code raw}, keeping only its digits.
     *
     * @param raw the telephone as supplied by the client, possibly {@code null}
     * @return the digits contained in {@code raw}; empty when {@code raw} is {@code null}
     *         or holds no digits
     */
    public String normalize(String raw) {
        return raw == null ? "" : raw.replaceAll("\\D", "");
    }

    /**
     * @param digits an already-normalized telephone
     * @return {@code true} when it consists of exactly {@link #REQUIRED_DIGITS} digits
     */
    public boolean hasRequiredLength(String digits) {
        return digits != null && digits.length() == REQUIRED_DIGITS;
    }
}
