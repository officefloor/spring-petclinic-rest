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

import java.util.Collection;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

/**
 * Decides whether a would-be owner shares a household with an existing one, i.e. has the
 * same last name and address. Both fields are compared case-insensitively and with runs of
 * whitespace collapsed to a single space, so cosmetic differences do not defeat the check.
 */
@Component
public class HouseholdDuplicateValidator {

    /**
     * @param candidate the owner about to be created
     * @param existingOwners owners already stored (typically pre-filtered by last name)
     * @return {@code true} when any existing owner has the same normalized last name and
     *         address as the candidate
     */
    public boolean sharesHouseholdWithExisting(Owner candidate, Collection<Owner> existingOwners) {
        String lastName = normalize(candidate.getLastName());
        String address = normalize(candidate.getAddress());
        return existingOwners.stream().anyMatch(existing ->
            normalize(existing.getLastName()).equals(lastName)
                && normalize(existing.getAddress()).equals(address));
    }

    /** Trim, collapse internal whitespace and lower-case, so equality ignores case and spacing. */
    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
