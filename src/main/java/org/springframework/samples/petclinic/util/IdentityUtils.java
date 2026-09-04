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

import java.util.Locale;

/**
 * Small stateless helpers for the string normalisation that backs the application's
 * identity comparisons.
 *
 * <p>Keeping the normalisation in one place means every identity comparison keys on the
 * same canonical form and they can never drift apart.
 */
public abstract class IdentityUtils {

    /**
     * Normalize a value for identity comparison: null becomes an empty string, surrounding
     * whitespace is trimmed, internal whitespace runs collapse to a single space, and the
     * result is lower-cased.
     *
     * @param value the raw value to normalize, possibly {@code null}
     * @return the canonical, lower-cased form, or the empty string if {@code value} is {@code null}
     */
    public static String normalizeIdentity(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
