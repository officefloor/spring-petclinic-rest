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

/**
 * Derives an owner's {@code membershipPoints} from their email address, namesake count,
 * household size and tenure.
 */
public final class MembershipPointsFormatter {

    /** Points awarded when an email address is on file. */
    private static final int EMAIL_POINTS = 2;

    /** Points awarded when the owner has no namesakes. */
    private static final int NO_NAMESAKE_POINTS = 1;

    /** Points awarded for belonging to a household of {@value #LARGE_HOUSEHOLD_SIZE} or more. */
    private static final int LARGE_HOUSEHOLD_POINTS = 2;

    /** Household size at or above which the large-household points are awarded. */
    private static final int LARGE_HOUSEHOLD_SIZE = 3;

    /** Points awarded once the owner's tenure exceeds {@value #TENURE_THRESHOLD_FISCAL_YEARS} fiscal year. */
    private static final int TENURE_POINTS = 3;

    /** Tenure, in elapsed fiscal years, an owner must exceed to earn the tenure points. */
    private static final int TENURE_THRESHOLD_FISCAL_YEARS = 1;

    private MembershipPointsFormatter() {
    }

    /**
     * Return the membership points: starting at {@code 0}, add {@value #EMAIL_POINTS} when an email
     * address is present, {@value #NO_NAMESAKE_POINTS} when the owner has no namesakes
     * ({@code namesakeCount} is {@code 0}), {@value #LARGE_HOUSEHOLD_POINTS} for a household of
     * {@value #LARGE_HOUSEHOLD_SIZE} or more members, and {@value #TENURE_POINTS} once the owner's
     * tenure exceeds {@value #TENURE_THRESHOLD_FISCAL_YEARS} fiscal year.
     *
     * @param namesakeCount    the number of owners sharing this owner's name, may be {@code null}
     * @param email            the owner's email address, may be {@code null}
     * @param householdSize    the number of members in the owner's household, may be {@code null}
     * @param registrationDate the date the owner was registered, may be {@code null}
     * @return the membership points, never negative
     */
    public static int format(Integer namesakeCount, String email, Integer householdSize,
            LocalDate registrationDate) {
        int points = 0;
        if (email != null && !email.isBlank()) {
            points += EMAIL_POINTS;
        }
        if (namesakeCount != null && namesakeCount == 0) {
            points += NO_NAMESAKE_POINTS;
        }
        if (householdSize != null && householdSize >= LARGE_HOUSEHOLD_SIZE) {
            points += LARGE_HOUSEHOLD_POINTS;
        }
        if (OwnerTenure.inFiscalYears(registrationDate) > TENURE_THRESHOLD_FISCAL_YEARS) {
            points += TENURE_POINTS;
        }
        return points;
    }
}
