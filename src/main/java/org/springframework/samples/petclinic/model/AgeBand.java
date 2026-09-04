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
import java.time.Period;

/**
 * Derives an owner's {@code ageBand} from a birth date, measured against the
 * owner's registration date: {@code MINOR} (under 18), {@code ADULT} (18-64) or
 * {@code SENIOR} (65+).
 */
public final class AgeBand {

    private AgeBand() {
    }

    /**
     * @param birthDate        the owner's date of birth, or {@code null}
     * @param registrationDate the date the age is measured against
     * @return the age band, or {@code null} when either date is absent
     */
    public static String of(LocalDate birthDate, LocalDate registrationDate) {
        if (birthDate == null || registrationDate == null) {
            return null;
        }
        int years = Period.between(birthDate, registrationDate).getYears();
        if (years < 18) {
            return "MINOR";
        }
        if (years < 65) {
            return "ADULT";
        }
        return "SENIOR";
    }
}
