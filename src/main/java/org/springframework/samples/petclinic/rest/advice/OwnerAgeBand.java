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
import java.time.Period;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code ageBand} from its birthDate measured against its registrationDate:
 * 'MINOR' (under 18), 'ADULT' (18-64) or 'SENIOR' (65+), or null when no birthDate is supplied.
 * Kept as a small standalone unit so the response mapper can expose the value without growing.
 */
public final class OwnerAgeBand {

    private OwnerAgeBand() {
    }

    public static String of(Owner owner) {
        LocalDate birthDate = owner.getBirthDate();
        LocalDate on = owner.getRegistrationDate();
        if (birthDate == null || on == null) {
            return null;
        }
        int years = Period.between(birthDate, on).getYears();
        if (years < 18) {
            return "MINOR";
        }
        return years < 65 ? "ADULT" : "SENIOR";
    }
}
