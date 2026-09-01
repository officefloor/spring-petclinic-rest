/*
 * Copyright 2016 the original author or authors.
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

package org.springframework.samples.petclinic.rest.advice;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code membershipPoints} and the {@code membershipLevel} mapped from
 * them. Points start at 0 and add 2 for a present email, 1 for a zero namesakeCount, 2 for
 * a household of 3 or more and 3 for tenure over 365 days from the registrationDate. The
 * level is 1 for 0-1 points, 2 for 2-3, 3 for 4-5 and 4 for 6 or more. Kept as a small
 * standalone unit so the response mapper can expose both values without growing.
 */
public final class OwnerMembershipLevel {

    private OwnerMembershipLevel() {
    }

    public static int points(Owner owner) {
        return (hasEmail(owner) ? 2 : 0)
            + (Integer.valueOf(0).equals(owner.getNamesakeCount()) ? 1 : 0)
            + (largeHousehold(owner) ? 2 : 0)
            + (tenuredOver365(owner) ? 3 : 0);
    }

    public static int of(Owner owner) {
        int p = points(owner);
        return p <= 1 ? 1 : p <= 3 ? 2 : p <= 5 ? 3 : 4;
    }

    private static boolean largeHousehold(Owner owner) {
        return owner.getHouseholdSize() != null && owner.getHouseholdSize() >= 3;
    }

    private static boolean hasEmail(Owner owner) {
        return owner.getEmail() != null && !owner.getEmail().isBlank();
    }

    private static boolean tenuredOver365(Owner owner) {
        LocalDate registered = owner.getRegistrationDate();
        return registered != null && ChronoUnit.DAYS.between(registered, LocalDate.now()) > 365;
    }
}
