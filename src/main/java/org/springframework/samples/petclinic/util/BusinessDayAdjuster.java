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

package org.springframework.samples.petclinic.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Adjustment of a date onto a business day. Kept separate from the owner controller and the
 * {@code Owner} model so the rule lives in one place, as a pure function with no web or persistence
 * dependencies. Mirrors {@link LocalityResolver}, which does the same for localities.
 *
 * <p>A business day is any weekday (Monday through Friday) that is not a listed public holiday. A
 * date that already falls on such a day is returned unchanged; a Saturday, Sunday or public holiday
 * is rolled forward one day at a time to the next non-holiday business day.
 */
public abstract class BusinessDayAdjuster {

    /**
     * The fixed public-holiday calendar. A date landing on one of these is treated like a weekend and
     * rolled forward to the next non-holiday business day.
     */
    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
        LocalDate.of(2026, 1, 1),
        LocalDate.of(2026, 1, 26),
        LocalDate.of(2026, 4, 25),
        LocalDate.of(2026, 12, 25),
        LocalDate.of(2026, 12, 28));

    /**
     * Roll a date forward onto the next business day, leaving weekdays that are not public holidays
     * unchanged.
     *
     * @param date the date to adjust (must not be null)
     * @return the same date when it is a non-holiday weekday, otherwise the next non-holiday business
     *         day
     */
    public static LocalDate toBusinessDay(LocalDate date) {
        while (isWeekend(date) || PUBLIC_HOLIDAYS.contains(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    private static boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

}
