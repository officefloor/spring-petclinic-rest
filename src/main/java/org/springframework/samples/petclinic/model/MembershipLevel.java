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
package org.springframework.samples.petclinic.model;

import java.time.LocalDate;

/**
 * Derives an owner's membership standing assigned on creation. {@code points} start at
 * {@code 0} and add {@code 2} for a present email, {@code 1} for a {@code namesakeCount}
 * of {@code 0}, {@code 2} for a household of at least three members and {@code 3} once the
 * owner's tenure exceeds one elapsed fiscal year. Those points map to a {@code level} of
 * {@code 1} ({@code 0-1}), {@code 2} ({@code 2-3}), {@code 3} ({@code 4-5}) or {@code 4}
 * ({@code 6} or more).
 */
public final class MembershipLevel {

    /** Elapsed fiscal years of tenure required before the tenure points are awarded. */
    private static final int TENURE_POINTS_YEARS = 1;

    private MembershipLevel() {
    }

    /**
     * @param namesakeCount        number of pre-existing namesakes when the owner was created
     * @param email                the owner's email, if any
     * @param householdMemberCount number of members in the owner's household
     * @param registrationDate     the date the owner was registered, used to derive tenure
     * @return the membership points, {@code 0} or more
     */
    public static int points(Integer namesakeCount, String email, Integer householdMemberCount,
            LocalDate registrationDate) {
        int points = 0;
        if (email != null && !email.isBlank()) {
            points += 2;
        }
        if (Integer.valueOf(0).equals(namesakeCount)) {
            points += 1;
        }
        if (householdMemberCount != null && householdMemberCount >= 3) {
            points += 2;
        }
        if (exceedsTenure(registrationDate)) {
            points += 3;
        }
        return points;
    }

    /**
     * @param points the membership points
     * @return the membership level from {@code 1} to {@code 4}
     */
    public static int of(int points) {
        if (points >= 6) {
            return 4;
        }
        if (points >= 4) {
            return 3;
        }
        if (points >= 2) {
            return 2;
        }
        return 1;
    }

    private static boolean exceedsTenure(LocalDate registrationDate) {
        return registrationDate != null
            && FiscalYear.elapsed(registrationDate, LocalDate.now()) > TENURE_POINTS_YEARS;
    }
}
