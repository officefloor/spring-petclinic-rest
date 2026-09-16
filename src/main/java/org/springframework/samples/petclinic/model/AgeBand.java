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
 * A coarse life-stage classification of a person: {@code MINOR} when under 18,
 * {@code ADULT} from 18 to 64, and {@code SENIOR} at 65 or older. Owns the age
 * boundaries so they live in one place rather than being scattered across callers.
 */
public enum AgeBand {

    MINOR, ADULT, SENIOR;

    private static final int ADULT_AGE = 18;
    private static final int SENIOR_AGE = 65;

    /**
     * Classify the band of someone born on {@code birthDate} as of {@code asOf},
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
