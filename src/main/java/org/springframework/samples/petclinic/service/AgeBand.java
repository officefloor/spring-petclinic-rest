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

import java.time.Period;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's age band from their birthDate, measured at registrationDate:
 * 'MINOR' (under 18), 'ADULT' (18-64) or 'SENIOR' (65+). Returns {@code null} when the
 * owner has no birthDate.
 */
public final class AgeBand {

    private AgeBand() {
    }

    public static String of(Owner owner) {
        if (owner.getBirthDate() == null) {
            return null;
        }
        int years = Period.between(owner.getBirthDate(), owner.getRegistrationDate()).getYears();
        if (years < 18) {
            return "MINOR";
        }
        return years < 65 ? "ADULT" : "SENIOR";
    }
}
