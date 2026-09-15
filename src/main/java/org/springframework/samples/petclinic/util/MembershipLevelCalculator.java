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

import org.springframework.samples.petclinic.model.Owner;

import java.time.LocalDate;

/**
 * Computes an owner's membership standing from its stored fields. An owner earns
 * {@link #membershipPoints(Owner) membership points} — starting at 0 and gaining
 * {@link #EMAIL_POINTS} for a present email, {@link #SOLE_NAMESAKE_POINTS} for having no
 * namesakes (a {@code namesakeCount} of 0), {@link #LARGE_HOUSEHOLD_POINTS} for a household of
 * {@link #LARGE_HOUSEHOLD_SIZE} or more, and {@link #TENURE_POINTS} once tenure exceeds
 * {@link #TENURE_THRESHOLD_FISCAL_YEARS} fiscal years. Those points map to a numeric
 * {@link #membershipLevel(Owner) membership level} from {@link #MIN_LEVEL} to {@link #MAX_LEVEL}.
 */
public abstract class MembershipLevelCalculator {

    /** Points awarded when the owner has an email address. */
    public static final int EMAIL_POINTS = 2;

    /** Points awarded when the owner has no namesakes (a {@code namesakeCount} of 0). */
    public static final int SOLE_NAMESAKE_POINTS = 1;

    /** Household size, in members, at or above which the large-household points are awarded. */
    public static final int LARGE_HOUSEHOLD_SIZE = 3;

    /** Points awarded when the owner belongs to a household of {@link #LARGE_HOUSEHOLD_SIZE} or more. */
    public static final int LARGE_HOUSEHOLD_POINTS = 2;

    /** Elapsed fiscal years an owner's tenure must exceed to earn the tenure points. */
    public static final long TENURE_THRESHOLD_FISCAL_YEARS = 1;

    /** Points awarded when the owner's tenure exceeds {@link #TENURE_THRESHOLD_FISCAL_YEARS} fiscal years. */
    public static final int TENURE_POINTS = 3;

    /** Lowest level this calculator awards, given to owners scoring 0-1 points. */
    public static final int MIN_LEVEL = 1;

    /** Highest level this calculator awards, reached once an owner scores 6 or more points. */
    public static final int MAX_LEVEL = 4;

    /**
     * Return the membership points for the given owner: 0 plus {@link #EMAIL_POINTS} when an
     * email is present, {@link #SOLE_NAMESAKE_POINTS} when {@code namesakeCount} is 0,
     * {@link #LARGE_HOUSEHOLD_POINTS} for a household of {@link #LARGE_HOUSEHOLD_SIZE} or more,
     * and {@link #TENURE_POINTS} when tenure exceeds {@link #TENURE_THRESHOLD_FISCAL_YEARS} fiscal years.
     */
    public static int membershipPoints(Owner owner) {
        int points = 0;
        if (owner.hasEmail()) {
            points += EMAIL_POINTS;
        }
        if (Integer.valueOf(0).equals(owner.getNamesakeCount())) {
            points += SOLE_NAMESAKE_POINTS;
        }
        if (hasLargeHousehold(owner)) {
            points += LARGE_HOUSEHOLD_POINTS;
        }
        if (hasQualifyingTenure(owner)) {
            points += TENURE_POINTS;
        }
        return points;
    }

    /**
     * Return the membership level for the given owner, mapped from its
     * {@link #membershipPoints(Owner) membership points}: level 1 for 0-1 points, 2 for 2-3,
     * 3 for 4-5, and {@link #MAX_LEVEL} for 6 or more.
     */
    public static int membershipLevel(Owner owner) {
        int points = membershipPoints(owner);
        return Math.min(MAX_LEVEL, MIN_LEVEL + points / 2);
    }

    /**
     * Whether the owner belongs to a household of {@link #LARGE_HOUSEHOLD_SIZE} or more members.
     * An owner with no recorded household size is treated as not qualifying.
     */
    private static boolean hasLargeHousehold(Owner owner) {
        Integer householdSize = owner.getHouseholdSize();
        return householdSize != null && householdSize >= LARGE_HOUSEHOLD_SIZE;
    }

    /**
     * Whether the owner's tenure, measured as the fiscal years elapsed from its registration
     * date to today, exceeds {@link #TENURE_THRESHOLD_FISCAL_YEARS}. An owner with no
     * registration date has no tenure.
     */
    private static boolean hasQualifyingTenure(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return false;
        }
        return FiscalYearResolver.elapsedYears(registrationDate, LocalDate.now())
            > TENURE_THRESHOLD_FISCAL_YEARS;
    }

}
