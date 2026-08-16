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
import java.time.temporal.ChronoUnit;

/**
 * Helpers for the numeric membership level a pet owner is assigned. The level is derived from the
 * owner's own fields together with their tenure (time elapsed since registration).
 */
public final class Memberships {

    /**
     * The lowest membership level every owner starts from.
     */
    public static final int BASE_LEVEL = 1;

    /**
     * The highest membership level reachable. Level 4 is only reached once an owner has qualifying
     * tenure (see {@link #TENURE_LEVEL_DAYS}) on top of the other factors.
     */
    public static final int MAX_LEVEL = 4;

    /**
     * The highest membership level derivable from an owner's own fields before tenure is considered.
     * A newly created owner has zero tenure, so it can never exceed this level.
     */
    public static final int PRE_TENURE_MAX_LEVEL = 3;

    /**
     * The tenure, in days, that must be strictly exceeded to earn the tenure factor. Membership
     * level 4 requires tenure of more than this many days; at or below it the tenure factor is not
     * granted.
     */
    public static final long TENURE_LEVEL_DAYS = 365;

    private Memberships() {
    }

    /**
     * Computes an owner's numeric membership level. Starts at {@link #BASE_LEVEL}; adds 1 when an
     * email is present; adds 1 when {@code namesakeCount} is 0; adds 1 when the owner's tenure is
     * more than {@link #TENURE_LEVEL_DAYS} days; capped at {@link #MAX_LEVEL}. Because a newly
     * created owner has zero tenure, it never earns the tenure factor and so never exceeds
     * {@link #PRE_TENURE_MAX_LEVEL}.
     *
     * @param email            the owner's email, or {@code null} when absent
     * @param namesakeCount    the owner's namesake count, or {@code null} when unknown
     * @param registrationDate the owner's registration date, or {@code null} when unknown; tenure is
     *                         measured from this date to today
     * @return the membership level, from {@value #BASE_LEVEL} to {@value #MAX_LEVEL}
     */
    public static int membershipLevel(String email, Integer namesakeCount, LocalDate registrationDate) {
        int level = BASE_LEVEL;
        if (email != null) {
            level++;
        }
        if (namesakeCount != null && namesakeCount == 0) {
            level++;
        }
        if (hasQualifyingTenure(registrationDate)) {
            level++;
        }
        return Math.min(level, MAX_LEVEL);
    }

    /**
     * Whether the given registration date represents tenure of more than {@link #TENURE_LEVEL_DAYS}
     * days as of today. A {@code null} (or future) registration date never qualifies.
     *
     * @param registrationDate the owner's registration date, or {@code null} when unknown
     * @return {@code true} when tenure strictly exceeds {@link #TENURE_LEVEL_DAYS} days
     */
    public static boolean hasQualifyingTenure(LocalDate registrationDate) {
        if (registrationDate == null) {
            return false;
        }
        return ChronoUnit.DAYS.between(registrationDate, LocalDate.now()) > TENURE_LEVEL_DAYS;
    }
}
