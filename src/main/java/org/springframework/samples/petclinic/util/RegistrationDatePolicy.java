/*
 * Copyright 2002-2017 the original author or authors.
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Resolves the effective registration date for an owner.
 *
 * <p>An owner is always registered on a business day. The supplied date defaults
 * to the server's current date when none was provided, and a date that falls on a
 * weekend (Saturday or Sunday) or a listed public holiday is rolled forward to the
 * next non-holiday business day. The resulting date is the single source of truth
 * for everything derived from the registration date (the stored value, the
 * membership number's year segment, and the per-day create-limit).
 */
public final class RegistrationDatePolicy {

    /**
     * The fixed list of public holidays that are not business days.
     */
    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
        LocalDate.of(2026, 1, 1),
        LocalDate.of(2026, 1, 26),
        LocalDate.of(2026, 4, 25),
        LocalDate.of(2026, 12, 25),
        LocalDate.of(2026, 12, 28)
    );

    private RegistrationDatePolicy() {
    }

    /**
     * Resolve the effective, business-day registration date.
     *
     * @param suppliedDate the date supplied in the request, or {@code null} to use
     *                     the server's current date
     * @return the supplied-or-defaulted date, rolled forward to the next business
     *         day when it falls on a weekend or a public holiday
     */
    public static LocalDate effectiveDate(LocalDate suppliedDate) {
        LocalDate date = suppliedDate != null ? suppliedDate : LocalDate.now();
        while (!isBusinessDay(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    /**
     * Tells whether a date is a business day: a weekday that is not a public holiday.
     *
     * @param date the date to test
     * @return {@code true} when the date is neither a weekend day nor a public holiday
     */
    private static boolean isBusinessDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY
            && dayOfWeek != DayOfWeek.SUNDAY
            && !PUBLIC_HOLIDAYS.contains(date);
    }

    /**
     * Tells whether a supplied registration date lies in the future relative to the
     * server's current date.
     *
     * @param suppliedDate the date supplied in the request, or {@code null} when omitted
     * @return {@code true} when a non-null date is later than the server's current date;
     *         {@code false} otherwise (an omitted date defaults to the server date and is
     *         therefore never in the future)
     */
    public static boolean isAfterServerDate(LocalDate suppliedDate) {
        return suppliedDate != null && suppliedDate.isAfter(LocalDate.now());
    }
}
