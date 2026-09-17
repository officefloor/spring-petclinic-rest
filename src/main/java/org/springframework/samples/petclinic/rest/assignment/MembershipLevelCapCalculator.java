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

package org.springframework.samples.petclinic.rest.assignment;

import java.util.List;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

/**
 * Computes the membership-level ceiling for a joining owner: a new owner may not rank more than one
 * level above their household, so the cap is one above the highest membership level currently held
 * by the owner's household members. When the owner has no existing household member there is no
 * ceiling.
 */
@Component
public class MembershipLevelCapCalculator {

    private final HouseholdMembers householdMembers;

    public MembershipLevelCapCalculator(HouseholdMembers householdMembers) {
        this.householdMembers = householdMembers;
    }

    /**
     * @param owner the owner being created, not yet saved
     * @return one above the maximum membership level among {@code owner}'s existing household
     * members, or {@code null} when the owner has no existing household member and so is uncapped
     */
    public Integer cap(Owner owner) {
        List<Owner> members = this.householdMembers.existing(owner);
        if (members.isEmpty()) {
            return null;
        }
        int max = 0;
        for (Owner member : members) {
            max = Math.max(max, member.getMembershipLevel());
        }
        return max + 1;
    }
}
