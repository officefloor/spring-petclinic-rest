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

package org.springframework.samples.petclinic.rest.advice;

import java.util.Collection;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Signals that a new owner shares an existing owner's last name and address
 * (compared case-insensitively with collapsed whitespace) without opting into a
 * shared household. Mapped to HTTP 409 Conflict by {@link ExceptionControllerAdvice}.
 */
public class DuplicateHouseholdException extends RuntimeException {

    public DuplicateHouseholdException(String lastName, String address) {
        super("Household already registered for: " + lastName + ", " + address);
    }

    /** Canonicalize a value so comparison ignores case and collapses whitespace runs. */
    private static String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * Throw if {@code candidate} has the same normalized last name and address as any owner in
     * {@code existingOwners}, unless {@code sharesHousehold} is set.
     */
    public static void rejectIfDuplicate(Owner candidate, boolean sharesHousehold, Collection<Owner> existingOwners) {
        if (sharesHousehold) {
            return;
        }
        String lastName = normalize(candidate.getLastName());
        String address = normalize(candidate.getAddress());
        for (Owner owner : existingOwners) {
            if (lastName.equals(normalize(owner.getLastName())) && address.equals(normalize(owner.getAddress()))) {
                throw new DuplicateHouseholdException(candidate.getLastName(), candidate.getAddress());
            }
        }
    }
}
