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

import java.time.Period;

/**
 * The age band an owner falls into, derived from their {@link Owner#getBirthDate() birthDate}
 * measured against their {@link Owner#getRegistrationDate() registrationDate}: {@link #MINOR}
 * when under 18, {@link #ADULT} from 18 to 64, {@link #SENIOR} at 65 or over.
 */
public enum AgeBand {

    /** Under 18 at registration. */
    MINOR,

    /** 18 to 64 at registration. */
    ADULT,

    /** 65 or over at registration. */
    SENIOR;

    /** The age at which an owner is no longer a {@link #MINOR}. */
    private static final int ADULT_AGE = 18;

    /** The age at which an owner becomes a {@link #SENIOR}. */
    private static final int SENIOR_AGE = 65;

    /**
     * Derive the age band for the given owner, using the completed years between their birth date
     * and registration date.
     *
     * @param owner the owner (must not be {@code null}).
     * @return the age band, or {@code null} when the owner has no birth date or no registration
     *         date to measure it against.
     */
    public static AgeBand forOwner(Owner owner) {
        if (owner.getBirthDate() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        int age = Period.between(owner.getBirthDate(), owner.getRegistrationDate()).getYears();
        if (age < ADULT_AGE) {
            return MINOR;
        }
        return age < SENIOR_AGE ? ADULT : SENIOR;
    }
}
