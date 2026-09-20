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

import org.springframework.stereotype.Component;

/**
 * Rolls a date forward onto a business day: when the given date falls on a Saturday or
 * Sunday it is advanced to the following Monday, otherwise it is returned unchanged.
 */
@Component
public class BusinessDayAdjuster {

    /**
     * Return {@code date} if it is a weekday, otherwise the next Monday.
     *
     * @param date the date to adjust
     * @return the same date when it is a business day, or the following Monday for a weekend date
     */
    public LocalDate toBusinessDay(LocalDate date) {
        LocalDate adjusted = date;
        while (adjusted.getDayOfWeek() == DayOfWeek.SATURDAY || adjusted.getDayOfWeek() == DayOfWeek.SUNDAY) {
            adjusted = adjusted.plusDays(1);
        }
        return adjusted;
    }
}
