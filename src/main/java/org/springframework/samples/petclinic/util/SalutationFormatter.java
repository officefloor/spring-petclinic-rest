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
 * Composes an owner's salutation from their optional title and last name.
 */
public final class SalutationFormatter {

    private SalutationFormatter() {
    }

    /**
     * Format the salutation as {@code "<title> <lastName>"}, or just {@code lastName}
     * when no title is given.
     *
     * @param title the courtesy title (e.g. {@code "MR"}, {@code "DR"}); may be {@code null} or blank
     * @param lastName the last name
     * @return the composed salutation
     */
    public static String format(String title, String lastName) {
        if (title == null || title.isBlank()) {
            return lastName;
        }
        return title + " " + lastName;
    }
}
