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
 * Classification of a person's age into a band: {@code MINOR} (under 18), {@code ADULT} (18-64)
 * or {@code SENIOR} (65+).
 */
public enum AgeBand {
    MINOR, ADULT, SENIOR;

    /** Inclusive lower bound (in years) at which a person is considered an adult. */
    private static final int ADULT_AGE = 18;

    /** Inclusive lower bound (in years) at which a person is considered a senior. */
    private static final int SENIOR_AGE = 65;

    /**
     * Return the age band of someone born on {@code birthDate} as evaluated on {@code asOf},
     * using their completed age in whole years.
     */
    public static AgeBand asOf(LocalDate birthDate, LocalDate asOf) {
        int years = Period.between(birthDate, asOf).getYears();
        if (years < ADULT_AGE) {
            return MINOR;
        }
        if (years < SENIOR_AGE) {
            return ADULT;
        }
        return SENIOR;
    }
}
