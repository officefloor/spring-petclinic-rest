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
import java.util.Set;

/**
 * The fixed list of public holidays observed when adjusting registration dates.
 *
 * <p>This is the single source of truth for which dates count as public holidays, so
 * {@link BusinessDay} can consult it without embedding the calendar itself.
 */
public final class PublicHolidays {

    private static final Set<LocalDate> HOLIDAYS = Set.of(
        LocalDate.of(2026, 1, 1),
        LocalDate.of(2026, 1, 26),
        LocalDate.of(2026, 4, 25),
        LocalDate.of(2026, 12, 25),
        LocalDate.of(2026, 12, 28));

    private PublicHolidays() {
    }

    /**
     * Whether {@code date} is a listed public holiday.
     *
     * @param date the date to test
     * @return {@code true} when {@code date} appears in the public-holiday list
     */
    public static boolean contains(LocalDate date) {
        return HOLIDAYS.contains(date);
    }
}
