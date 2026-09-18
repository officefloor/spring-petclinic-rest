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
 * The band a person falls into based on their completed age in years, as measured on a
 * given reference date: {@link #MINOR} for under 18, {@link #ADULT} for 18 to 64, and
 * {@link #SENIOR} for 65 and over.
 */
public enum AgeBand {

    MINOR,
    ADULT,
    SENIOR;

    /**
     * The band for someone born on {@code birthDate} as measured on {@code asOf}, using the
     * completed number of whole years between the two dates.
     *
     * @param birthDate the date of birth
     * @param asOf      the reference date the age is measured against
     * @return the matching age band
     */
    public static AgeBand asOf(LocalDate birthDate, LocalDate asOf) {
        int years = Period.between(birthDate, asOf).getYears();
        if (years < 18) {
            return MINOR;
        }
        if (years < 65) {
            return ADULT;
        }
        return SENIOR;
    }
}
