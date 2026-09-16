/*
 * Copyright 2002-2017 the original author or authors.
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
package org.springframework.samples.petclinic.model;

import java.time.LocalDate;

/**
 * Business-day rules for registration dates.
 *
 * <p>A registration date must fall on a business day: any Saturday, Sunday, or
 * {@link PublicHolidays public holiday} rolls forward to the next date that is none of
 * those, while an ordinary weekday is left unchanged. This is the single source of truth
 * for that adjustment, so callers never repeat it.
 */
public final class BusinessDay {

    private BusinessDay() {
    }

    /**
     * Roll a date forward onto a business day.
     *
     * @param date the date to adjust
     * @return the earliest date on or after {@code date} that is neither a weekend nor a
     *         public holiday
     */
    public static LocalDate rollForward(LocalDate date) {
        LocalDate adjusted = date;
        while (!isBusinessDay(adjusted)) {
            adjusted = adjusted.plusDays(1);
        }
        return adjusted;
    }

    private static boolean isBusinessDay(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case SATURDAY, SUNDAY -> false;
            default -> !PublicHolidays.contains(date);
        };
    }
}
