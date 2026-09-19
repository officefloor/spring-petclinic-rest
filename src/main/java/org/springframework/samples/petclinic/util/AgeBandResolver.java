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
 * Derives an owner's age band from their birth date, measured as at their registration date:
 * {@code MINOR} for under 18s, {@code ADULT} for 18 to 64 and {@code SENIOR} for 65 and over.
 */
public final class AgeBandResolver {

    /** Age band for owners younger than {@link #ADULT_AGE} at registration. */
    private static final String MINOR = "MINOR";

    /** Age band for owners aged {@link #ADULT_AGE} up to (but not including) {@link #SENIOR_AGE}. */
    private static final String ADULT = "ADULT";

    /** Age band for owners aged {@link #SENIOR_AGE} or older at registration. */
    private static final String SENIOR = "SENIOR";

    /** Lower bound (inclusive) of the {@link #ADULT} band. */
    private static final int ADULT_AGE = 18;

    /** Lower bound (inclusive) of the {@link #SENIOR} band. */
    private static final int SENIOR_AGE = 65;

    private AgeBandResolver() {
    }

    /**
     * Resolve the owner's age band as at their registration date.
     *
     * @param birthDate        the owner's date of birth, may be {@code null}
     * @param registrationDate the date the owner was registered, may be {@code null}
     * @return {@code "MINOR"}, {@code "ADULT"} or {@code "SENIOR"}, or {@code null} when either
     *         date is missing
     */
    public static String resolve(LocalDate birthDate, LocalDate registrationDate) {
        if (birthDate == null || registrationDate == null) {
            return null;
        }
        int age = Period.between(birthDate, registrationDate).getYears();
        if (age < ADULT_AGE) {
            return MINOR;
        }
        return age < SENIOR_AGE ? ADULT : SENIOR;
    }
}
