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
package org.springframework.samples.petclinic.util;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Normalizes free-text values so they can be compared for equality regardless of surrounding or
 * repeated whitespace and letter case. Leading and trailing whitespace is trimmed, every internal
 * run of whitespace is collapsed to a single space, and the result is lower-cased.
 */
public final class TextNormalizer {

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private TextNormalizer() {
    }

    /**
     * Produces a canonical form of the supplied value for case-insensitive, whitespace-insensitive
     * comparison.
     *
     * @param value the raw value, may be {@code null}
     * @return the normalized value, or an empty string when {@code value} is {@code null}
     */
    public static String normalizeForComparison(String value) {
        if (value == null) {
            return "";
        }
        return WHITESPACE.matcher(value.trim()).replaceAll(" ").toLowerCase(Locale.ROOT);
    }
}
