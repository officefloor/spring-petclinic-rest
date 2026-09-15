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

package org.springframework.samples.petclinic.util;

import java.time.LocalDate;
import java.time.Period;

/**
 * Resolves an age band from a birth date measured against a reference date
 * (an owner's registration date): {@code MINOR} when under 18, {@code ADULT}
 * from 18 to 64, and {@code SENIOR} at 65 or older.
 */
public abstract class AgeBandResolver {

    /**
     * Return the age band for the given birth date as of the reference date, or
     * {@code null} when either date is absent.
     */
    public static String bandOf(LocalDate birthDate, LocalDate asOf) {
        if (birthDate == null || asOf == null) {
            return null;
        }
        int years = Period.between(birthDate, asOf).getYears();
        if (years < 18) {
            return "MINOR";
        }
        if (years < 65) {
            return "ADULT";
        }
        return "SENIOR";
    }

}
