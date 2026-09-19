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
import java.time.temporal.ChronoUnit;

/**
 * Derives an owner's tenure: the number of whole days elapsed since their registration date.
 */
public final class OwnerTenure {

    private OwnerTenure() {
    }

    /**
     * Return the owner's tenure in whole days, measured from their registration date to today.
     * A missing registration date, or one that has not yet arrived, yields a tenure of {@code 0}.
     *
     * @param registrationDate the date the owner was registered, may be {@code null}
     * @return the tenure in days, never negative
     */
    public static long inDays(LocalDate registrationDate) {
        if (registrationDate == null) {
            return 0;
        }
        long days = ChronoUnit.DAYS.between(registrationDate, LocalDate.now());
        return Math.max(days, 0);
    }
}
