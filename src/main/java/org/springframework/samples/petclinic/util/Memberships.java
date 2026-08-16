/*
 * Copyright 2016-2017 the original author or authors.
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

/**
 * Helpers for the membership points and numeric level a pet owner is assigned. Points are derived
 * from the owner's own fields together with their tenure (elapsed fiscal years since registration),
 * and the level is a banding of those points.
 */
public final class Memberships {

    /**
     * The lowest membership level every owner starts from.
     */
    public static final int BASE_LEVEL = 1;

    /**
     * The highest membership level reachable. Level 4 is only reached with 6 or more points, and the
     * only route to 6 points is qualifying tenure (see {@link #TENURE_LEVEL_FISCAL_YEARS}) on top of
     * the other factors.
     */
    public static final int MAX_LEVEL = 4;

    /**
     * The points added when an email is present.
     */
    public static final int EMAIL_POINTS = 2;

    /**
     * The points added when {@code namesakeCount} is 0.
     */
    public static final int UNIQUE_NAME_POINTS = 1;

    /**
     * The points added for a household of {@link #HOUSEHOLD_POINTS_THRESHOLD} or more.
     */
    public static final int HOUSEHOLD_POINTS = 2;

    /**
     * The household size at or above which {@link #HOUSEHOLD_POINTS} are added.
     */
    public static final int HOUSEHOLD_POINTS_THRESHOLD = 3;

    /**
     * The points added for tenure over {@link #TENURE_LEVEL_FISCAL_YEARS} elapsed fiscal years.
     */
    public static final int TENURE_POINTS = 3;

    /**
     * The tenure, in elapsed fiscal years (see
     * {@link org.springframework.samples.petclinic.util.FiscalYears#elapsedFiscalYears}), that must
     * be strictly exceeded to earn the tenure points. At or below it the tenure points are not
     * granted.
     */
    public static final int TENURE_LEVEL_FISCAL_YEARS = 1;

    private Memberships() {
    }

    /**
     * Computes an owner's membership points. Starts at 0; adds {@link #EMAIL_POINTS} when an email is
     * present; adds {@link #UNIQUE_NAME_POINTS} when {@code namesakeCount} is 0; adds
     * {@link #HOUSEHOLD_POINTS} for a household of {@link #HOUSEHOLD_POINTS_THRESHOLD} or more; adds
     * {@link #TENURE_POINTS} when the owner's tenure is more than {@link #TENURE_LEVEL_FISCAL_YEARS}
     * elapsed fiscal years.
     *
     * @param email            the owner's email, or {@code null} when absent
     * @param namesakeCount    the owner's namesake count, or {@code null} when unknown
     * @param householdSize    the owner's household size, or {@code null} when unknown
     * @param registrationDate the owner's registration date, or {@code null} when unknown; tenure is
     *                         measured from this date to today
     * @return the membership points, 0 or more
     */
    public static int membershipPoints(String email, Integer namesakeCount, Integer householdSize,
            LocalDate registrationDate) {
        int points = 0;
        if (email != null) {
            points += EMAIL_POINTS;
        }
        if (namesakeCount != null && namesakeCount == 0) {
            points += UNIQUE_NAME_POINTS;
        }
        if (householdSize != null && householdSize >= HOUSEHOLD_POINTS_THRESHOLD) {
            points += HOUSEHOLD_POINTS;
        }
        if (hasQualifyingTenure(registrationDate)) {
            points += TENURE_POINTS;
        }
        return points;
    }

    /**
     * Maps membership points to a numeric membership level: 1 (0-1 points), 2 (2-3), 3 (4-5),
     * 4 (6 or more).
     *
     * @param points the owner's membership points (see {@link #membershipPoints})
     * @return the membership level, from {@value #BASE_LEVEL} to {@value #MAX_LEVEL}
     */
    public static int membershipLevel(int points) {
        if (points >= 6) {
            return 4;
        }
        if (points >= 4) {
            return 3;
        }
        if (points >= 2) {
            return 2;
        }
        return BASE_LEVEL;
    }

    /**
     * Computes an owner's numeric membership level from their fields, by mapping
     * {@link #membershipPoints} through {@link #membershipLevel(int)}.
     *
     * @param email            the owner's email, or {@code null} when absent
     * @param namesakeCount    the owner's namesake count, or {@code null} when unknown
     * @param householdSize    the owner's household size, or {@code null} when unknown
     * @param registrationDate the owner's registration date, or {@code null} when unknown; tenure is
     *                         measured from this date to today
     * @return the membership level, from {@value #BASE_LEVEL} to {@value #MAX_LEVEL}
     */
    public static int membershipLevel(String email, Integer namesakeCount, Integer householdSize,
            LocalDate registrationDate) {
        return membershipLevel(membershipPoints(email, namesakeCount, householdSize, registrationDate));
    }

    /**
     * Whether the given registration date represents tenure of more than
     * {@link #TENURE_LEVEL_FISCAL_YEARS} elapsed fiscal years as of today. A {@code null} (or future)
     * registration date never qualifies.
     *
     * @param registrationDate the owner's registration date, or {@code null} when unknown
     * @return {@code true} when tenure strictly exceeds {@link #TENURE_LEVEL_FISCAL_YEARS} elapsed
     *         fiscal years
     */
    public static boolean hasQualifyingTenure(LocalDate registrationDate) {
        if (registrationDate == null) {
            return false;
        }
        return FiscalYears.elapsedFiscalYears(registrationDate, LocalDate.now()) > TENURE_LEVEL_FISCAL_YEARS;
    }
}
