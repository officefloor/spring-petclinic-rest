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

import java.time.Period;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Derives an owner's age band from its {@code birthDate}, computed as full years
 * against the {@code registrationDate}: {@code MINOR} when under 18, {@code ADULT}
 * from 18 to 64 inclusive, {@code SENIOR} at 65 or over. Yields {@code null} when
 * no birth date was supplied.
 */
public final class AgeBand {

    private AgeBand() {
    }

    /**
     * @param owner the owner whose age band to derive (its {@code birthDate} and
     *              {@code registrationDate} decide the band).
     * @return the age band, or {@code null} when the owner has no birth date.
     */
    public static OwnerDto.AgeBandEnum of(Owner owner) {
        if (owner.getBirthDate() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        int years = Period.between(owner.getBirthDate(), owner.getRegistrationDate()).getYears();
        if (years < 18) {
            return OwnerDto.AgeBandEnum.MINOR;
        }
        if (years < 65) {
            return OwnerDto.AgeBandEnum.ADULT;
        }
        return OwnerDto.AgeBandEnum.SENIOR;
    }
}
