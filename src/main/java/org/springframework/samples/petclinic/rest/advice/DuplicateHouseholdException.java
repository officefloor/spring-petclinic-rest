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

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.Household;

/**
 * Signals that a new owner shares an existing owner's household (the same last name
 * and postcode, via their computed {@code householdId}) without opting into a shared
 * household. Mapped to HTTP 409 Conflict by {@link ExceptionControllerAdvice}.
 */
public class DuplicateHouseholdException extends RuntimeException {

    public DuplicateHouseholdException(String lastName, String postcode) {
        super("Household already registered for: " + lastName + ", " + postcode);
    }

    /**
     * Throw if {@code candidate} shares an existing owner's {@code householdId} (same last name
     * and postcode), unless {@code sharesHousehold} is set.
     */
    public static void rejectIfDuplicate(Owner candidate, boolean sharesHousehold, Collection<Owner> existingOwners) {
        if (sharesHousehold) {
            return;
        }
        String household = Household.idFor(candidate);
        for (Owner owner : existingOwners) {
            if (owner.isDeleted()) {
                continue;
            }
            if (household.equals(Household.idFor(owner))) {
                throw new DuplicateHouseholdException(candidate.getLastName(), candidate.getPostcode());
            }
        }
    }
}
