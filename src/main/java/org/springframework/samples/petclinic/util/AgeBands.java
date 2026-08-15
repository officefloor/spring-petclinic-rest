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
import java.time.Period;

/**
 * Helpers for the age band a pet owner falls into, derived from their birth date relative to their
 * registration date.
 */
public final class AgeBands {

    /**
     * The age (inclusive) at which an owner is no longer a {@code MINOR}.
     */
    public static final int ADULT_AGE = 18;

    /**
     * The age (inclusive) at which an owner becomes a {@code SENIOR}.
     */
    public static final int SENIOR_AGE = 65;

    private AgeBands() {
    }

    /**
     * Computes an owner's age band from their birth date relative to their registration date:
     * {@code 'MINOR'} when under {@value #ADULT_AGE}, {@code 'ADULT'} from {@value #ADULT_AGE} to
     * {@value #SENIOR_AGE} minus one, {@code 'SENIOR'} at {@value #SENIOR_AGE} or older. Returns
     * {@code null} when either date is absent (no birth date was supplied).
     *
     * @param birthDate        the owner's birth date, or {@code null} when absent
     * @param registrationDate the owner's registration date the age is measured against
     * @return the age band, or {@code null} when it cannot be derived
     */
    public static String ageBand(LocalDate birthDate, LocalDate registrationDate) {
        if (birthDate == null || registrationDate == null) {
            return null;
        }
        int age = Period.between(birthDate, registrationDate).getYears();
        if (age < ADULT_AGE) {
            return "MINOR";
        }
        if (age < SENIOR_AGE) {
            return "ADULT";
        }
        return "SENIOR";
    }
}
