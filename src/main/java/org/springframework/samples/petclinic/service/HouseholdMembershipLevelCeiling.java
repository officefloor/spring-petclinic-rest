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
import org.springframework.stereotype.Component;

/**
 * Computes the membership-level ceiling for a new owner: its
 * {@link Owner#getMembershipLevel() membership level} may not exceed one above
 * the highest membership level among the existing members of its household.
 */
@Component
public class HouseholdMembershipLevelCeiling {

    private final HouseholdMembers householdMembers;

    public HouseholdMembershipLevelCeiling(HouseholdMembers householdMembers) {
        this.householdMembers = householdMembers;
    }

    /**
     * The highest membership level the given new owner may reach, being one above
     * the current maximum membership level among its household's members.
     *
     * @param owner the owner being registered
     * @return the ceiling to apply, or {@code null} when the household has no
     *         existing member and therefore no cap applies
     */
    public Integer ceilingFor(Owner owner) {
        return householdMembers.of(owner).stream()
            .map(Owner::getMembershipLevel)
            .max(Integer::compareTo)
            .map(max -> max + 1)
            .orElse(null);
    }
}
