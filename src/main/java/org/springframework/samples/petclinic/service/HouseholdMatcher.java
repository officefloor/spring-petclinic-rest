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
package org.springframework.samples.petclinic.service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Detects owners that belong to the same household: identical last name and address
 * once case and surrounding/duplicate whitespace are normalised.
 */
public final class HouseholdMatcher {

    private HouseholdMatcher() {
    }

    public static boolean sameHousehold(Owner a, Owner b) {
        return normalize(a.getLastName()).equals(normalize(b.getLastName()))
            && normalize(a.getAddress()).equals(normalize(b.getAddress()));
    }

    /**
     * A stable identifier shared by every owner in the same household. Derived purely from
     * the normalised last name and address, so any two same-household owners produce the
     * same value without coordination.
     */
    public static String householdId(Owner owner) {
        String key = normalize(owner.getLastName()) + "\n" + normalize(owner.getAddress());
        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8)).toString();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
