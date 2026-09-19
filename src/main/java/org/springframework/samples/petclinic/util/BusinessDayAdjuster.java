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
import java.time.temporal.TemporalAdjusters;

/**
 * Adjusts dates so that they fall on a business day (Monday to Friday).
 */
public final class BusinessDayAdjuster {

    private BusinessDayAdjuster() {
    }

    /**
     * Return the given date if it already falls on a business day, otherwise roll it forward to the
     * next Monday. A Saturday or Sunday is rolled forward; any weekday is returned unchanged.
     *
     * @param date the date to adjust
     * @return the date rolled forward to the next Monday when it lands on a weekend, or {@code null}
     *     if {@code date} is {@code null}
     */
    public static LocalDate toBusinessDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return date.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        }
        return date;
    }
}
