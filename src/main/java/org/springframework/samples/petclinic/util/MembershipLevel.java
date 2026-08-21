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

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.FiscalYear;

/**
 * Derives an owner's membership from a points system. Starts at 0 points, adds 2 when an
 * email is present, adds 1 when {@code namesakeCount} is 0, adds 2 for a household of 3 or
 * more, and adds 3 when tenure exceeds {@value #TENURE_FISCAL_YEARS} elapsed fiscal year(s).
 * Points map to a numeric level: 1 for 0-1 points, 2 for 2-3, 3 for 4-5, and 4 for 6 or more.
 * Because level 4 requires 6 points and the only route to 6 is tenure, a newly created owner
 * (zero tenure) never exceeds level 3.
 */
public final class MembershipLevel {

    /** Household size, in members, at or above which the household factor applies. */
    private static final int HOUSEHOLD_SIZE = 3;

    /** Tenure, in elapsed fiscal years, that must be exceeded for the tenure factor to apply. */
    private static final int TENURE_FISCAL_YEARS = 1;

    private MembershipLevel() {
    }

    /**
     * @param owner the owner whose points to derive (its {@code email},
     *              {@code namesakeCount}, {@code householdSize} and tenure decide the points).
     * @return the membership points, 0 or more.
     */
    public static int points(Owner owner) {
        int points = 0;
        if (owner.getEmail() != null && !owner.getEmail().isEmpty()) {
            points += 2;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            points += 1;
        }
        if (owner.getHouseholdSize() != null && owner.getHouseholdSize() >= HOUSEHOLD_SIZE) {
            points += 2;
        }
        if (tenureFiscalYears(owner) > TENURE_FISCAL_YEARS) {
            points += 3;
        }
        return points;
    }

    /**
     * @param owner the owner whose level to derive.
     * @return the membership level, from 1 to 4, mapped from {@link #points(Owner)}, then
     *         capped by the owner's {@code membershipLevelCap} when one is set (see
     *         {@code AssignMembershipCap}). A null cap leaves the level unchanged.
     */
    public static int of(Owner owner) {
        int level = level(points(owner));
        Integer cap = owner.getMembershipLevelCap();
        if (cap != null && level > cap) {
            return cap;
        }
        return level;
    }

    /** Maps points to the uncapped numeric level: 1 for 0-1 points, 2 for 2-3, 3 for 4-5, 4 for 6+. */
    private static int level(int points) {
        if (points <= 1) {
            return 1;
        }
        if (points <= 3) {
            return 2;
        }
        if (points <= 5) {
            return 3;
        }
        return 4;
    }

    /**
     * Elapsed fiscal years from the owner's {@code registrationDate} to today (the number of
     * 1-July boundaries crossed since registration), or 0 when unknown.
     */
    private static long tenureFiscalYears(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return 0;
        }
        return FiscalYear.elapsed(registrationDate, LocalDate.now());
    }
}
