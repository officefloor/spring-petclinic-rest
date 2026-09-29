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
package org.springframework.samples.petclinic.service;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.validation.RegistrationDateNormalizer;

/**
 * The membership-scoring business rules for an {@link Owner}. Kept as a standalone domain helper so
 * both the service (when capping a new owner's level against their household) and the presentation
 * mapper (when exposing the derived fields) share one definition rather than duplicating the ladder.
 */
public final class MembershipLevels {

    private MembershipLevels() {
    }

    /**
     * The owner's membership points. Starts at {@code 0}; adds {@code 2} when an email address is
     * present; adds {@code 1} when the owner has no namesakes (namesakeCount is 0); adds {@code 2}
     * when the owner belongs to a household of {@code 3} or more members; adds {@code 3} when the
     * owner's tenure exceeds one fiscal year, where tenure is the number of elapsed fiscal years
     * (the current fiscal year minus the registration fiscal year).
     */
    public static Integer points(Owner owner) {
        if (owner == null) {
            return null;
        }
        int points = 0;
        String email = owner.getEmail();
        if (email != null && !email.isEmpty()) {
            points += 2;
        }
        Integer namesakeCount = owner.getNamesakeCount();
        if (namesakeCount != null && namesakeCount == 0) {
            points += 1;
        }
        Integer householdMemberCount = owner.getHouseholdMemberCount();
        if (householdMemberCount != null && householdMemberCount >= 3) {
            points += 2;
        }
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate != null) {
            int tenure = RegistrationDateNormalizer.fiscalYear(LocalDate.now())
                - RegistrationDateNormalizer.fiscalYear(registrationDate);
            if (tenure > 1) {
                points += 3;
            }
        }
        return points;
    }

    /**
     * The owner's numeric membership level derived from {@link #points(Owner)}: level {@code 1} for
     * {@code 0-1} points, level {@code 2} for {@code 2-3} points, level {@code 3} for {@code 4-5}
     * points, and level {@code 4} for {@code 6} or more points. The result is then capped so it
     * cannot exceed the owner's {@code membershipLevelCap} (one above the maximum membership level
     * among the household members present when the owner was created); a {@code null} cap — set when
     * the owner had no existing household member — leaves the level uncapped.
     */
    public static Integer level(Owner owner) {
        if (owner == null) {
            return null;
        }
        int points = points(owner);
        int level;
        if (points <= 1) {
            level = 1;
        } else if (points <= 3) {
            level = 2;
        } else if (points <= 5) {
            level = 3;
        } else {
            level = 4;
        }
        Integer cap = owner.getMembershipLevelCap();
        if (cap != null && level > cap) {
            level = cap;
        }
        return level;
    }
}
