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
import java.time.Month;

/**
 * Resolves fiscal-year values for dates. The fiscal year starts on 1 July and is
 * identified by the calendar year in which it ends, so 1 July 2026 to 30 June 2027
 * is fiscal year 2027. Dates on or after 1 July belong to the next calendar year's
 * fiscal year; earlier dates belong to the current calendar year's.
 */
public abstract class FiscalYearResolver {

    /** Month on whose first day each fiscal year begins. */
    public static final Month FISCAL_YEAR_START_MONTH = Month.JULY;

    /**
     * Return the fiscal year the given date falls in, identified by the calendar year in
     * which the fiscal year ends: the date's own year for dates before 1 July, or the year
     * after for dates on or after 1 July.
     */
    public static int yearOf(LocalDate date) {
        int year = date.getYear();
        return date.getMonthValue() >= FISCAL_YEAR_START_MONTH.getValue() ? year + 1 : year;
    }

    /**
     * Return the fiscal-year label for the given date, formatted {@code 'FY<YY>'} where
     * {@code YY} is the last two digits of the {@link #yearOf(LocalDate) fiscal year},
     * e.g. {@code "FY27"}. Returns {@code null} when the date is absent.
     */
    public static String label(LocalDate date) {
        if (date == null) {
            return null;
        }
        return String.format("FY%02d", yearOf(date) % 100);
    }

    /**
     * Return the number of whole fiscal years elapsed between {@code from} and {@code to},
     * as the difference of their {@link #yearOf(LocalDate) fiscal years}. Negative when
     * {@code to} precedes {@code from}'s fiscal year.
     */
    public static long elapsedYears(LocalDate from, LocalDate to) {
        return (long) yearOf(to) - yearOf(from);
    }

}
