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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Utility for aligning dates to business days.
 */
public final class BusinessDay {

    /**
     * Fixed public holidays that are not business days. A registration date landing on one of
     * these rolls forward to the next non-holiday weekday.
     */
    private static final Set<LocalDate> HOLIDAYS = Set.of(
            LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-26"),
            LocalDate.parse("2026-04-25"), LocalDate.parse("2026-12-25"), LocalDate.parse("2026-12-28"));

    private BusinessDay() {
    }

    /**
     * Roll {@code date} forward to the next business day: any Saturday, Sunday or listed public
     * holiday moves to the following non-holiday weekday, while any other weekday is returned
     * unchanged.
     *
     * @param date the date to align
     * @return the same date when it already falls on a business day, otherwise the next business day
     */
    public static LocalDate rollForward(LocalDate date) {
        LocalDate rolled = date;
        while (!isBusinessDay(rolled)) {
            rolled = rolled.plusDays(1);
        }
        return rolled;
    }

    private static boolean isBusinessDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY && !HOLIDAYS.contains(date);
    }
}
