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
 * Derives an owner's age band from its birth date as at its registration date:
 * {@link #MINOR} when under 18, {@link #ADULT} from 18 to 64, and {@link #SENIOR}
 * at 65 or older.
 */
public final class AgeBand {

    /** Age band for owners under 18 at registration. */
    public static final String MINOR = "MINOR";

    /** Age band for owners aged 18 to 64 at registration. */
    public static final String ADULT = "ADULT";

    /** Age band for owners aged 65 or older at registration. */
    public static final String SENIOR = "SENIOR";

    private AgeBand() {
    }

    /**
     * Returns the owner's age band derived from its birth date as at its registration
     * date, or {@code null} when the owner, its birth date or its registration date is
     * absent.
     */
    public static String forOwner(Owner owner) {
        if (owner == null) {
            return null;
        }
        return forDates(owner.getBirthDate(), owner.getRegistrationDate());
    }

    /**
     * Returns the age band for someone born on {@code birthDate} as at {@code asOf}, or
     * {@code null} when either date is {@code null}.
     */
    public static String forDates(LocalDate birthDate, LocalDate asOf) {
        if (birthDate == null || asOf == null) {
            return null;
        }
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
