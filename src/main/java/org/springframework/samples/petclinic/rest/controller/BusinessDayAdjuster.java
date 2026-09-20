/*
 * Copyright 2016 the original author or authors.
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

package org.springframework.samples.petclinic.rest.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

import org.springframework.stereotype.Component;

/**
 * Rolls a date forward onto a business day: when the given date falls on a Saturday, a
 * Sunday or a listed public holiday it is advanced to the next non-holiday weekday,
 * otherwise it is returned unchanged.
 */
@Component
public class BusinessDayAdjuster {

    /** Fixed public holidays that the business-day roll skips. */
    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
            LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-26"),
            LocalDate.parse("2026-04-25"), LocalDate.parse("2026-12-25"), LocalDate.parse("2026-12-28"));

    /**
     * Return {@code date} if it is a business day, otherwise the next non-holiday weekday.
     *
     * @param date the date to adjust
     * @return the same date when it is a business day, or the next weekday that is neither a
     *         weekend nor a public holiday
     */
    public LocalDate toBusinessDay(LocalDate date) {
        LocalDate adjusted = date;
        while (!isBusinessDay(adjusted)) {
            adjusted = adjusted.plusDays(1);
        }
        return adjusted;
    }

    private boolean isBusinessDay(LocalDate date) {
        return date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY
                && !PUBLIC_HOLIDAYS.contains(date);
    }
}
