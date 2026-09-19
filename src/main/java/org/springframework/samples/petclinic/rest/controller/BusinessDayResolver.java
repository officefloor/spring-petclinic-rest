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
import java.time.temporal.TemporalAdjusters;

import org.springframework.stereotype.Component;

/**
 * Resolves a date to the business day on which it takes effect.
 *
 * <p>A Saturday or Sunday is rolled forward to the following Monday; any weekday is
 * returned unchanged. This is applied to an owner's effective registration date so that
 * everything derived from it (the stored {@code registrationDate}, the membership
 * number's year segment, and the per-day create limit) uses a business day.
 */
@Component
public class BusinessDayResolver {

    /**
     * Roll {@code date} forward to a business day.
     *
     * @param date the date to resolve
     * @return {@code date} itself when it is a weekday, otherwise the next Monday
     */
    public LocalDate toBusinessDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return date.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        }
        return date;
    }
}
