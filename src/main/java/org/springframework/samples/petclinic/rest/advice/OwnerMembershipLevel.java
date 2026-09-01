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
 * Derives an owner's {@code membershipLevel}. Base factors (a present email and a zero
 * namesakeCount) lift a member from level 1 up to level 3; reaching level 4 additionally
 * requires tenure of more than 365 days measured from the registrationDate. A brand-new
 * owner has zero tenure, so it never exceeds level 3. Kept as a small standalone unit so
 * the response mapper can expose the value without growing.
 */
public final class OwnerMembershipLevel {

    private OwnerMembershipLevel() {
    }

    public static int of(Owner owner) {
        int base = 1 + (hasEmail(owner) ? 1 : 0) + (Integer.valueOf(0).equals(owner.getNamesakeCount()) ? 1 : 0);
        return Math.min(3, base) + (tenuredOver365(owner) ? 1 : 0);
    }

    private static boolean hasEmail(Owner owner) {
        return owner.getEmail() != null && !owner.getEmail().isBlank();
    }

    private static boolean tenuredOver365(Owner owner) {
        LocalDate registered = owner.getRegistrationDate();
        return registered != null && ChronoUnit.DAYS.between(registered, LocalDate.now()) > 365;
    }
}
