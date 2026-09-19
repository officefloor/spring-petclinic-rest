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
 * Resolves a date to the business day on which it takes effect.
 *
 * <p>A Saturday, Sunday, or listed public holiday is rolled forward to the next
 * non-holiday weekday; any other weekday is returned unchanged. This is applied to an
 * owner's effective registration date so that everything derived from it (the stored
 * {@code registrationDate}, the membership number's year segment, and the per-day create
 * limit) uses a business day.
 */
@Component
public class BusinessDayResolver {

    /** Fixed public holidays that are not business days. */
    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
        LocalDate.parse("2026-01-01"),
        LocalDate.parse("2026-01-26"),
        LocalDate.parse("2026-04-25"),
        LocalDate.parse("2026-12-25"),
        LocalDate.parse("2026-12-28"));

    /**
     * Roll {@code date} forward to a business day.
     *
     * @param date the date to resolve
     * @return {@code date} itself when it is a non-holiday weekday, otherwise the next
     *     non-holiday business day
     */
    public LocalDate toBusinessDay(LocalDate date) {
        while (!isBusinessDay(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    private boolean isBusinessDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY
            && !PUBLIC_HOLIDAYS.contains(date);
    }
}
