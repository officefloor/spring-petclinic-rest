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
import java.time.temporal.ChronoUnit;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's numeric membership level. Starts at 1, adds 1 when an email is
 * present, adds 1 when {@code namesakeCount} is 0, and adds 1 when tenure exceeds
 * {@value #TENURE_DAYS} days. Level 4 requires that tenure: because a newly created
 * owner has zero tenure, a new owner never exceeds level 3.
 */
public final class MembershipLevel {

    /** Highest level this derivation produces. */
    private static final int CAP = 4;

    /** Tenure, in days, that must be exceeded for the level-4 factor to apply. */
    private static final int TENURE_DAYS = 365;

    private MembershipLevel() {
    }

    /**
     * @param owner the owner whose level to derive (its {@code email},
     *              {@code namesakeCount} and tenure decide the level).
     * @return the membership level, from 1 to {@value #CAP}.
     */
    public static int of(Owner owner) {
        int level = 1;
        if (owner.getEmail() != null && !owner.getEmail().isEmpty()) {
            level++;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            level++;
        }
        if (tenureDays(owner) > TENURE_DAYS) {
            level++;
        }
        return Math.min(level, CAP);
    }

    /** Days from the owner's {@code registrationDate} to today, or 0 when unknown. */
    private static long tenureDays(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(registrationDate, LocalDate.now());
    }
}
