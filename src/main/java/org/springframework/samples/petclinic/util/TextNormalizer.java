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

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Reduces free-text values to a canonical form for case- and whitespace-insensitive
 * comparison: leading/trailing whitespace is trimmed, internal runs of whitespace are
 * collapsed to a single space, and the result is lower-cased. Two values that differ only
 * in letter casing or in how their whitespace is laid out normalize to the same string.
 */
public final class TextNormalizer {

    /** Any run of whitespace, collapsed to a single space. */
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private TextNormalizer() {
    }

    /**
     * Normalize the given value for comparison.
     *
     * @param value the raw value (may be {@code null})
     * @return the trimmed, whitespace-collapsed, lower-cased value, or {@code null} when
     * {@code value} is {@code null}
     */
    public static String normalize(String value) {
        if (value == null) {
            return null;
        }
        return WHITESPACE.matcher(value.strip()).replaceAll(" ").toLowerCase(Locale.ROOT);
    }
}
