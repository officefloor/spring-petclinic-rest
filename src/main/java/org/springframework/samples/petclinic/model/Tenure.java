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

/**
 * How long an owner has been a member, measured in whole elapsed fiscal years from their
 * {@link Owner#getRegistrationDate() registrationDate} to the current date (see
 * {@link FiscalYear}). An owner registered within the current fiscal year has zero tenure,
 * and an owner without a registration date has none.
 */
public final class Tenure {

    private Tenure() {
    }

    /**
     * Derive the owner's tenure as the number of whole fiscal years elapsed since registration.
     *
     * @param owner the owner (must not be {@code null}).
     * @return the number of elapsed fiscal years since registration, or {@code 0} when the owner
     *         has no registration date (or has not yet reached it).
     */
    public static long inFiscalYears(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return 0;
        }
        return FiscalYear.yearsBetween(registrationDate, LocalDate.now());
    }
}
