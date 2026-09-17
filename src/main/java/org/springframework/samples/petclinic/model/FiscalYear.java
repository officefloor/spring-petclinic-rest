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
package org.springframework.samples.petclinic.model;

import java.time.LocalDate;
import java.time.Month;

/**
 * Fiscal-year arithmetic for date-derived owner values. The fiscal year starts on
 * {@value #START_MONTH_VALUE} July and is identified by the calendar year in which it ends,
 * so 1 July 2025 to 30 June 2026 is fiscal year 2026, labelled {@code 'FY26'}.
 *
 * <p>The single source of truth for anything that maps a date onto a fiscal year: the
 * membership number's year segment, the returned {@code fiscalYear} label and elapsed-tenure
 * counting.
 */
public final class FiscalYear {

    /** The month (1-based) on whose first day the fiscal year starts. */
    public static final int START_MONTH_VALUE = Month.JULY.getValue();

    private FiscalYear() {
    }

    /**
     * The fiscal year containing the given date, identified by the calendar year in which it
     * ends (a date in or after July belongs to the fiscal year ending the following calendar
     * year).
     *
     * @param date the date (must not be {@code null}).
     * @return the ending calendar year of the fiscal year that contains the date.
     */
    public static int endingYear(LocalDate date) {
        return date.getMonthValue() >= START_MONTH_VALUE ? date.getYear() + 1 : date.getYear();
    }

    /**
     * The fiscal-year label {@code 'FY<YY>'} for the given date, where YY is the last two digits
     * of the {@link #endingYear(LocalDate) ending year}.
     *
     * @param date the date, or {@code null}.
     * @return the label, or {@code null} when the date is {@code null}.
     */
    public static String label(LocalDate date) {
        return date == null ? null : "FY" + twoDigitYear(date);
    }

    /**
     * The last two digits of the {@link #endingYear(LocalDate) ending year}, zero-padded.
     *
     * @param date the date (must not be {@code null}).
     * @return the two-digit ending-year segment (e.g. {@code '26'}).
     */
    public static String twoDigitYear(LocalDate date) {
        return String.format("%02d", endingYear(date) % 100);
    }

    /**
     * The number of whole fiscal years elapsed between two dates: how many 1 July boundaries lie
     * between them, never negative.
     *
     * @param from the earlier date (must not be {@code null}).
     * @param to   the later date (must not be {@code null}).
     * @return the count of elapsed fiscal years, or {@code 0} when {@code to} precedes {@code from}.
     */
    public static long yearsBetween(LocalDate from, LocalDate to) {
        return Math.max(0, endingYear(to) - endingYear(from));
    }
}
