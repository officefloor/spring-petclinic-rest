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

/**
 * Adjusts dates so that they fall on a business day: a weekday (Monday to Friday) that is not a
 * listed public holiday.
 */
public final class BusinessDayAdjuster {

    private BusinessDayAdjuster() {
    }

    /**
     * Return the given date if it already falls on a business day, otherwise roll it forward to the
     * next business day. A Saturday, Sunday or {@link PublicHolidays public holiday} is rolled
     * forward one day at a time until a non-holiday weekday is reached.
     *
     * @param date the date to adjust
     * @return the date rolled forward to the next business day, or {@code null} if {@code date} is
     *     {@code null}
     */
    public static LocalDate toBusinessDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        while (!isBusinessDay(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    private static boolean isBusinessDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY
                && !PublicHolidays.isHoliday(date);
    }
}
