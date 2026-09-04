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

package org.springframework.samples.petclinic.util;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Adjustment of a date onto a business day. Kept separate from the owner controller and the
 * {@code Owner} model so the rule lives in one place, as a pure function with no web or persistence
 * dependencies. Mirrors {@link LocalityResolver}, which does the same for localities.
 *
 * <p>A business day is any weekday (Monday through Friday). A date that already falls on a weekday is
 * returned unchanged; a Saturday or Sunday is rolled forward to the following Monday.
 */
public abstract class BusinessDayAdjuster {

    /**
     * Roll a date forward onto the next business day, leaving weekdays unchanged.
     *
     * @param date the date to adjust (must not be null)
     * @return the same date when it is a weekday, otherwise the following Monday
     */
    public static LocalDate toBusinessDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY) {
            return date.plusDays(2);
        }
        if (dayOfWeek == DayOfWeek.SUNDAY) {
            return date.plusDays(1);
        }
        return date;
    }

}
