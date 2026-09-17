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

package org.springframework.samples.petclinic.rest.validation;

import java.util.Locale;

import org.springframework.stereotype.Component;

/**
 * Canonical identity of a household. Two owners belong to the same household when they share a last
 * name and address, compared case-insensitively and with collapsed whitespace. This class is the
 * single place that decides that equivalence so duplicate detection and household-id assignment stay
 * in agreement.
 */
@Component
public class HouseholdKey {

    /**
     * @param lastName the owner's last name
     * @param address  the owner's address
     * @return a canonical key such that owners in the same household produce equal keys
     */
    public String of(String lastName, String address) {
        // Separate the two parts with a newline: normalization collapses all whitespace, so a
        // newline cannot appear inside either part and the boundary is unambiguous.
        return normalize(lastName) + "\n" + normalize(address);
    }

    /** Lower-case and collapse runs of whitespace to a single space so trivial formatting
     *  differences do not defeat the household comparison. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
