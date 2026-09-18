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

import java.util.List;

/**
 * Finds the members of an owner's household — the owners that share its
 * {@link Owner#getHouseholdId() household id}.
 *
 * <p>Household members always share a last name (the household id is derived
 * from the last name and postcode), so members are found via the same
 * case-insensitive last-name lookup used elsewhere and then narrowed to the
 * matching household id.
 */
@Component
public class HouseholdMembers {

    private final OwnerRepository ownerRepository;

    public HouseholdMembers(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    /**
     * The existing owners that share the given owner's household. Empty when the
     * owner has no household id. For an owner being created (not yet persisted)
     * this is exactly the household's current members.
     *
     * @param owner the owner whose household members are wanted
     * @return the matching owners, or an empty list when the owner has no household
     */
    public List<Owner> of(Owner owner) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return List.of();
        }
        return ownerRepository.findByLastNameIgnoreCase(owner.getLastName()).stream()
            .filter(other -> householdId.equals(other.getHouseholdId()))
            .toList();
    }
}
