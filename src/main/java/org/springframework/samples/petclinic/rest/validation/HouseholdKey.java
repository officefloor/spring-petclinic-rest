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
 * The canonical identity of a household: an owner's last name together with the address they live
 * at. Two owners belong to the same household when their keys are equal. Both parts are normalized
 * (trimmed, internal whitespace runs collapsed to a single space, lower-cased) so values that differ
 * only in letter case or spacing match. This shared definition keeps household matching consistent
 * between the uniqueness check and the shared-identifier assignment.
 */
public final class HouseholdKey {

    private HouseholdKey() {
    }

    /**
     * The comparison key for the household an owner with this last name and address belongs to.
     */
    public static String of(String lastName, String address) {
        return normalize(lastName) + "\n" + normalize(address);
    }

    /**
     * A stable identifier for a household key: the first 12 upper-case hex characters of its
     * SHA-256 digest. Deterministic, so every member of a household derives the same value.
     */
    public static String idFor(String key) {
        return Sha256.hex(key).substring(0, 12).toUpperCase(Locale.ROOT);
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
