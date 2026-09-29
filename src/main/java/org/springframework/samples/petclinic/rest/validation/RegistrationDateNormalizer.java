/*
 * Copyright 2016-2017 the original author or authors.
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

package org.springframework.samples.petclinic.rest.validation;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Utility for normalizing an owner's registration date to a business day. The effective
 * registration date must never fall on a weekend or a public holiday, so a Saturday, Sunday
 * or listed public holiday is rolled forward to the next non-holiday business day; a weekday
 * value that is not a holiday is left untouched. This applies to the effective date whether
 * it was supplied in the request or defaulted to the server date, and any value derived from
 * the registration date (such as the membership number's year segment) uses the adjusted date.
 */
public final class RegistrationDateNormalizer {

    /** Fixed list of public holidays that the registration date must roll past. */
    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
        LocalDate.of(2026, 1, 1),
        LocalDate.of(2026, 1, 26),
        LocalDate.of(2026, 4, 25),
        LocalDate.of(2026, 12, 25),
        LocalDate.of(2026, 12, 28));

    private RegistrationDateNormalizer() {
    }

    /**
     * Normalize the supplied registration date to a business day.
     *
     * @param date the effective registration date (must not be {@code null})
     * @return the same date when it falls on a non-holiday weekday, otherwise the next
     *         non-holiday business day
     */
    public static LocalDate normalize(LocalDate date) {
        LocalDate normalized = date;
        while (isWeekend(normalized) || PUBLIC_HOLIDAYS.contains(normalized)) {
            normalized = normalized.plusDays(1);
        }
        return normalized;
    }

    private static boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }
}
