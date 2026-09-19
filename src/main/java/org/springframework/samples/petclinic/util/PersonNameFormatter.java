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
 * Formats a person's stored first and last names into a single display string.
 */
public final class PersonNameFormatter {

    private PersonNameFormatter() {
    }

    /**
     * Format the given names as {@code "LastName, FirstName"}.
     *
     * @param firstName the first name (may be {@code null})
     * @param lastName the last name (may be {@code null})
     * @return the names formatted as {@code "LastName, FirstName"}
     */
    public static String displayName(String firstName, String lastName) {
        return lastName + ", " + firstName;
    }

    /**
     * Format the upper-cased first letters of the given names as {@code "F.L."}.
     *
     * @param firstName the first name
     * @param lastName the last name
     * @return the initials formatted as {@code "F.L."}
     */
    public static String initials(String firstName, String lastName) {
        return initial(firstName) + initial(lastName);
    }

    private static String initial(String name) {
        return Character.toUpperCase(name.charAt(0)) + ".";
    }
}
