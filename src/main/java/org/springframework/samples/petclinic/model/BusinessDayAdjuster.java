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

package org.springframework.samples.petclinic.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Rolls a date forward onto a business day: any Saturday, Sunday, or listed
 * public holiday is advanced to the next non-holiday weekday, while an ordinary
 * business day is returned unchanged.
 */
public final class BusinessDayAdjuster {

    /** Fixed public holidays that are skipped when rolling to a business day. */
    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
            LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-26"),
            LocalDate.parse("2026-04-25"), LocalDate.parse("2026-12-25"),
            LocalDate.parse("2026-12-28"));

    private BusinessDayAdjuster() {
    }

    /**
     * Adjusts the given date to a business day.
     *
     * @param date the date to adjust
     * @return the same date when it is a non-holiday weekday, otherwise the next
     *         non-holiday weekday
     */
    public static LocalDate toBusinessDay(LocalDate date) {
        while (isNonBusinessDay(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    private static boolean isNonBusinessDay(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY
                || PUBLIC_HOLIDAYS.contains(date);
    }
}
