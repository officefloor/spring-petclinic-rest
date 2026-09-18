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
 * A person's age band on a reference date: {@link #MINOR} (under 18),
 * {@link #ADULT} (18 to 64) or {@link #SENIOR} (65 or older).
 *
 * <p>This enum owns the age-to-band classification: given a birth date and the
 * date to measure against, it computes the completed-years age and maps it to
 * the band that age falls in.
 */
public enum AgeBand {

    MINOR,
    ADULT,
    SENIOR;

    /** Youngest age, in completed years, that counts as an adult rather than a minor. */
    private static final int ADULT_AGE = 18;

    /** Youngest age, in completed years, that counts as a senior. */
    private static final int SENIOR_AGE = 65;

    /**
     * Classify the age band for someone born on {@code birthDate} as measured on
     * {@code referenceDate}.
     *
     * @param birthDate     the date of birth, or {@code null} when unknown
     * @param referenceDate the date to measure the age against, or {@code null} when unknown
     * @return the age band, or {@code null} when either date is {@code null}
     */
    public static AgeBand on(LocalDate birthDate, LocalDate referenceDate) {
        if (birthDate == null || referenceDate == null) {
            return null;
        }
        int age = Period.between(birthDate, referenceDate).getYears();
        if (age < ADULT_AGE) {
            return MINOR;
        }
        if (age < SENIOR_AGE) {
            return ADULT;
        }
        return SENIOR;
    }
}
