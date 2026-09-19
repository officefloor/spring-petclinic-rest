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

import java.time.LocalDate;
import java.util.Set;

/**
 * The fixed list of public holidays observed by the clinic. On these dates the clinic is closed, so
 * they do not count as business days.
 */
public final class PublicHolidays {

    private static final Set<LocalDate> DATES = Set.of(
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 26),
            LocalDate.of(2026, 4, 25),
            LocalDate.of(2026, 12, 25),
            LocalDate.of(2026, 12, 28));

    private PublicHolidays() {
    }

    /**
     * @param date the date to test
     * @return {@code true} if the given date is a listed public holiday
     */
    public static boolean isHoliday(LocalDate date) {
        return DATES.contains(date);
    }
}
