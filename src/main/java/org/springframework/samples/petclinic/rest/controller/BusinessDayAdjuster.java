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
 * Rolls a date forward onto a business day.
 * <p>
 * Saturdays, Sundays and listed public holidays are not business days. Any such date is rolled
 * forward one day at a time until it lands on a non-holiday weekday. A date that already falls on
 * a business day is returned unchanged.
 */
@Component
public class BusinessDayAdjuster {

    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
        LocalDate.of(2026, 1, 1),
        LocalDate.of(2026, 1, 26),
        LocalDate.of(2026, 4, 25),
        LocalDate.of(2026, 12, 25),
        LocalDate.of(2026, 12, 28));

    /**
     * Returns {@code date} if it is a business day, otherwise the next non-holiday weekday.
     *
     * @param date the date to adjust
     * @return the same date when it is a business day, else the next business day
     */
    public LocalDate toBusinessDay(LocalDate date) {
        LocalDate adjusted = date;
        while (!isBusinessDay(adjusted)) {
            adjusted = adjusted.plusDays(1);
        }
        return adjusted;
    }

    private boolean isBusinessDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY
            && dayOfWeek != DayOfWeek.SUNDAY
            && !PUBLIC_HOLIDAYS.contains(date);
    }
}
