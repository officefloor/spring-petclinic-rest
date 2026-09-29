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

import java.util.Locale;

/**
 * Utility for canonicalizing an owner identity field (such as last name or address) so two
 * values can be compared for the household-duplicate rule. Comparison is case-insensitive and
 * ignores differences in surrounding and internal whitespace: the value is trimmed, internal
 * whitespace runs are collapsed to a single space, and the result is lower-cased. A {@code null}
 * value normalizes to an empty string.
 */
public final class IdentityNormalizer {

    private IdentityNormalizer() {
    }

    /**
     * Normalize the supplied identity value for equality comparison.
     *
     * @param value the raw identity value (may be {@code null})
     * @return the trimmed, whitespace-collapsed, lower-cased value, or an empty string when {@code null}
     */
    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
