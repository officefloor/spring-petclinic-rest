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
package org.springframework.samples.petclinic.util;

import java.time.LocalDate;
import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Membership points/level scoring and the household level ceiling. Kept as small pure functions so
 * both the owner mapper (for display) and the create path (to freeze a capped level) share one source.
 */
public final class MembershipLevels {

    private MembershipLevels() {
    }

    /** Membership points: +2 for an email, +1 when namesakeCount is 0, +2 for a household of 3 or more, +3 for tenure over 365 days. */
    public static int points(Owner owner) {
        int points = 0;
        if (owner.getEmail() != null) {
            points += 2;
        }
        if (Integer.valueOf(0).equals(owner.getNamesakeCount())) {
            points += 1;
        }
        if (owner.getHouseholdMemberCount() != null && owner.getHouseholdMemberCount() >= 3) {
            points += 2;
        }
        if (tenureFiscalYears(owner) >= 1) {
            points += 3;
        }
        return points;
    }

    /** Numeric level derived from membershipPoints: 1 (0-1), 2 (2-3), 3 (4-5), 4 (6 or more). */
    public static int level(Owner owner) {
        int points = points(owner);
        if (points <= 1) {
            return 1;
        }
        if (points <= 3) {
            return 2;
        }
        return points <= 5 ? 3 : 4;
    }

    /**
     * The level a new owner may be given: at most one above the maximum level among the existing
     * (non-deleted) members of their household. With no existing household member no cap applies and
     * the owner's own level is returned.
     */
    public static Integer cap(Owner owner, Collection<Owner> existing) {
        String householdId = owner.getHouseholdId();
        int maxOther = Integer.MIN_VALUE;
        for (Owner other : existing) {
            if (householdId != null && householdId.equals(other.getHouseholdId()) && !other.isDeleted()) {
                maxOther = Math.max(maxOther, level(other));
            }
        }
        int own = level(owner);
        return maxOther == Integer.MIN_VALUE ? own : Math.min(own, maxOther + 1);
    }

    private static int tenureFiscalYears(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return 0;
        }
        return FiscalYears.elapsed(registrationDate, LocalDate.now());
    }
}
