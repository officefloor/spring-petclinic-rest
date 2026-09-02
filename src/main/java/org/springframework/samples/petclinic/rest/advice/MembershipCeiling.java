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

import org.springframework.samples.petclinic.model.Household;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.util.FiscalYear;

import java.time.LocalDate;
import java.util.Collection;
import java.util.OptionalInt;

/**
 * Caps a newcomer's membershipLevel at one above the highest level already held by an existing member
 * of their household (same last name and postcode). With no other household member the level is left
 * untouched. Each existing member's level is scored the same way {@link MembershipPoints} scores it.
 */
final class MembershipCeiling {

    private MembershipCeiling() {
    }

    /** The newcomer's level, lowered to at most one above the maximum among their household members. */
    static int cap(int level, OwnerDto newcomer, Collection<Owner> allOwners) {
        String household = Household.id(newcomer.getLastName(), newcomer.getPostcode());
        if (household == null) {
            return level;
        }
        OptionalInt ceiling = allOwners.stream()
            .filter(o -> o.getId() != null && !o.getId().equals(newcomer.getId()))
            .filter(o -> household.equals(Household.id(o.getLastName(), o.getPostcode())))
            .mapToInt(member -> memberLevel(member, allOwners))
            .max();
        return ceiling.isPresent() ? Math.min(level, ceiling.getAsInt() + 1) : level;
    }

    /** The membershipLevel an existing household member currently holds, by the same scoring rules. */
    private static int memberLevel(Owner member, Collection<Owner> allOwners) {
        boolean hasEmail = member.getEmail() != null && !member.getEmail().isBlank();
        String household = Household.id(member.getLastName(), member.getPostcode());
        long householdSize = household == null ? 0 : allOwners.stream()
            .filter(o -> household.equals(Household.id(o.getLastName(), o.getPostcode())))
            .count();
        LocalDate registered = member.getRegistrationDate();
        boolean longTenure = registered != null && FiscalYear.elapsed(registered, LocalDate.now()) > 1;
        int points = (hasEmail ? 2 : 0) + (firstOfName(member, allOwners) ? 1 : 0)
            + (householdSize >= 3 ? 2 : 0) + (longTenure ? 3 : 0);
        return points >= 6 ? 4 : points >= 4 ? 3 : points >= 2 ? 2 : 1;
    }

    /** True when no earlier-registered owner shares the member's first and last name (namesakeCount 0). */
    private static boolean firstOfName(Owner member, Collection<Owner> allOwners) {
        return allOwners.stream().noneMatch(o -> o.getId() != null && member.getId() != null
            && o.getId() < member.getId() && sameName(o.getFirstName(), member.getFirstName())
            && sameName(o.getLastName(), member.getLastName()));
    }

    private static boolean sameName(String a, String b) {
        return a != null && a.equalsIgnoreCase(b);
    }
}
