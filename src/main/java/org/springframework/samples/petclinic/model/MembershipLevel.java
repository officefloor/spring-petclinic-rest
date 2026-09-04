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
import java.time.temporal.ChronoUnit;

/**
 * Derives an owner's numeric {@code membershipLevel} assigned on creation: starts at
 * {@code 1}, adds {@code 1} each for a present email, a {@code namesakeCount} of
 * {@code 0} and a household of at least three members. These pre-tenure factors are
 * capped at {@code 3}; level {@code 4} is reached only once the owner's tenure exceeds
 * {@code 365} days, so a newly created (zero-tenure) owner never exceeds {@code 3}.
 */
public final class MembershipLevel {

    /** Days of tenure required before an owner may reach level {@code 4}. */
    private static final long LEVEL_4_TENURE_DAYS = 365;

    private MembershipLevel() {
    }

    /**
     * @param namesakeCount        number of pre-existing namesakes when the owner was created
     * @param email                the owner's email, if any
     * @param householdMemberCount number of members in the owner's household
     * @param registrationDate     the date the owner was registered, used to derive tenure
     * @return the membership level from {@code 1} to {@code 4}
     */
    public static int of(Integer namesakeCount, String email, Integer householdMemberCount,
            LocalDate registrationDate) {
        int level = 1;
        if (email != null && !email.isBlank()) {
            level++;
        }
        if (Integer.valueOf(0).equals(namesakeCount)) {
            level++;
        }
        if (householdMemberCount != null && householdMemberCount >= 3) {
            level++;
        }
        if (level >= 4 && exceedsLevel4Tenure(registrationDate)) {
            return 4;
        }
        return Math.min(level, 3);
    }

    private static boolean exceedsLevel4Tenure(LocalDate registrationDate) {
        return registrationDate != null
            && ChronoUnit.DAYS.between(registrationDate, LocalDate.now()) > LEVEL_4_TENURE_DAYS;
    }
}
