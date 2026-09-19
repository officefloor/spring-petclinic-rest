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
 * Derives an owner's {@code membershipLevel} from their namesake count, email address and tenure.
 */
public final class MembershipLevelFormatter {

    /** The level every owner starts at. */
    private static final int BASE_LEVEL = 1;

    /** Highest level reachable from email and namesake status alone; the top level is tenure-gated. */
    private static final int PRE_TENURE_MAX_LEVEL = 3;

    /** Tenure, in days, an owner must exceed to reach the top membership level. */
    private static final int TENURE_THRESHOLD_DAYS = 365;

    private MembershipLevelFormatter() {
    }

    /**
     * Return the membership level: starting at {@value #BASE_LEVEL}, add 1 when an email address is
     * present and add 1 when the owner has no namesakes ({@code namesakeCount} is {@code 0}), capped
     * at {@value #PRE_TENURE_MAX_LEVEL}; then add 1 more (reaching level 4) once the owner's tenure
     * exceeds {@value #TENURE_THRESHOLD_DAYS} days. A newly registered owner has zero tenure, so a
     * new owner never exceeds level {@value #PRE_TENURE_MAX_LEVEL}.
     *
     * @param namesakeCount    the number of owners sharing this owner's name, may be {@code null}
     * @param email            the owner's email address, may be {@code null}
     * @param registrationDate the date the owner was registered, may be {@code null}
     * @return the membership level, between {@value #BASE_LEVEL} and {@value #PRE_TENURE_MAX_LEVEL}
     *         plus 1 when the tenure threshold is exceeded
     */
    public static int format(Integer namesakeCount, String email, LocalDate registrationDate) {
        int level = BASE_LEVEL;
        if (email != null && !email.isBlank()) {
            level++;
        }
        if (namesakeCount != null && namesakeCount == 0) {
            level++;
        }
        level = Math.min(level, PRE_TENURE_MAX_LEVEL);
        if (OwnerTenure.inDays(registrationDate) > TENURE_THRESHOLD_DAYS) {
            level++;
        }
        return level;
    }
}
