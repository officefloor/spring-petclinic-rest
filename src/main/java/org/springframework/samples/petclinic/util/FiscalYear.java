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

import java.time.LocalDate;

/**
 * Derives fiscal-year values from calendar dates. The fiscal year starts on 1 July and is
 * identified by the calendar year in which it ends, so 1 July 2025 to 30 June 2026 is fiscal
 * year 2026. A date on or after 1 July therefore belongs to the fiscal year of the next calendar
 * year, while a date before 1 July belongs to the fiscal year of its own calendar year.
 */
public final class FiscalYear {

    private FiscalYear() {
    }

    /**
     * Returns the fiscal year (the ending calendar year) that the given date falls in. A date in
     * July or later belongs to the following calendar year's fiscal year; an earlier date belongs
     * to its own calendar year's fiscal year.
     *
     * @param date the date to classify (must not be {@code null})
     * @return the four-digit fiscal year the date falls in
     */
    public static int of(LocalDate date) {
        return date.getMonthValue() >= 7 ? date.getYear() + 1 : date.getYear();
    }

    /**
     * Returns the fiscal year of the given date formatted {@code 'FY<YY>'}, where YY is the last
     * two digits of the fiscal year (e.g. {@code 'FY26'}).
     *
     * @param date the date to classify (must not be {@code null})
     * @return the formatted fiscal-year label
     */
    public static String label(LocalDate date) {
        return String.format("FY%02d", of(date) % 100);
    }

    /**
     * Returns the number of whole fiscal years that have elapsed between the two dates, i.e. the
     * difference between the fiscal year of {@code to} and the fiscal year of {@code from}.
     *
     * @param from the earlier date (must not be {@code null})
     * @param to   the later date (must not be {@code null})
     * @return the count of elapsed fiscal years (may be zero or negative)
     */
    public static int elapsed(LocalDate from, LocalDate to) {
        return of(to) - of(from);
    }
}
