/*
 * Copyright 2002-2017 the original author or authors.
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

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.stereotype.Component;

/**
 * Counts the members of an owner's household — the owners that share its
 * {@link Owner#getHouseholdId() household id}.
 *
 * <p>Household members always share a last name (the household id is derived
 * from the last name and postcode), so members are found via the same
 * case-insensitive last-name lookup used elsewhere and then narrowed to the
 * matching household id.
 */
@Component
public class HouseholdMemberCounter {

    private final OwnerRepository ownerRepository;

    public HouseholdMemberCounter(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    /**
     * Count the members of the given owner's household, including the owner
     * itself. An owner with no household id is a household of one.
     *
     * @param owner the owner being registered
     * @return the number of household members once the owner is included
     */
    public int count(Owner owner) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return 1;
        }
        long others = ownerRepository.findByLastNameIgnoreCase(owner.getLastName()).stream()
            .filter(other -> householdId.equals(other.getHouseholdId()))
            .count();
        return (int) others + 1;
    }
}
