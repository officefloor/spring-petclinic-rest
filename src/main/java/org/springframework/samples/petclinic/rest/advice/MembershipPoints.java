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

/**
 * Scores an owner's membership: points start at 0 and gain +2 for an email, +1 when namesakeCount
 * is 0, +2 for a household of 3 or more members and +3 for tenure over one fiscal year. The points map
 * to a membershipLevel of 1 (0-1 points), 2 (2-3), 3 (4-5) or 4 (6 or more). Both are set on the response.
 */
final class MembershipPoints {

    private MembershipPoints() {
    }

    static void assign(OwnerDto owner, int namesakeCount, Collection<Owner> allOwners) {
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        int points = (hasEmail ? 2 : 0) + (namesakeCount == 0 ? 1 : 0)
            + (householdSize(owner, allOwners) >= 3 ? 2 : 0) + (longTenure(owner) ? 3 : 0);
        owner.setMembershipPoints(points);
        owner.setMembershipLevel(points >= 6 ? 4 : points >= 4 ? 3 : points >= 2 ? 2 : 1);
    }

    /** The number of owners sharing this owner's household (same last name and postcode). */
    private static long householdSize(OwnerDto owner, Collection<Owner> allOwners) {
        String household = Household.id(owner.getLastName(), owner.getPostcode());
        return household == null ? 0 : allOwners.stream()
            .filter(o -> household.equals(Household.id(o.getLastName(), o.getPostcode())))
            .count();
    }

    /** True once the owner's tenure exceeds one fiscal year; a new owner's zero tenure counts as false. */
    private static boolean longTenure(OwnerDto owner) {
        LocalDate registered = owner.getRegistrationDate();
        return registered != null && FiscalYear.elapsed(registered, LocalDate.now()) > 1;
    }
}
