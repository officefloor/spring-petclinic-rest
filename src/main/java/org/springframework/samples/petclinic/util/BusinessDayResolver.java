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
 * Resolves the business day for a given date. A weekend (Saturday or Sunday) or a
 * listed public holiday rolls forward to the next non-holiday weekday; any other
 * weekday is returned unchanged.
 */
public abstract class BusinessDayResolver {

    /** Fixed public holidays that are not treated as business days. */
    private static final Set<LocalDate> HOLIDAYS = Set.of(
            LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 26), LocalDate.of(2026, 4, 25),
            LocalDate.of(2026, 12, 25), LocalDate.of(2026, 12, 28));

    /**
     * Return the given date if it is a weekday that is not a public holiday, otherwise
     * roll forward day by day to the next non-holiday business day.
     */
    public static LocalDate toBusinessDay(LocalDate date) {
        while (isWeekend(date) || HOLIDAYS.contains(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    private static boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

}
