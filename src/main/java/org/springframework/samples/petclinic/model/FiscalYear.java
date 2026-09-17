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
import java.time.Month;

/**
 * Fiscal-year calendar rules. The fiscal year runs from 1 July to 30 June and is identified by
 * the calendar year in which it ends: a date on or after 1 July belongs to the fiscal year that
 * ends in the following calendar year (e.g. 18 September 2026 is in the fiscal year ending 2027).
 */
public final class FiscalYear {

    /** The month on whose first day each fiscal year starts. */
    private static final Month START = Month.JULY;

    private FiscalYear() {
    }

    /**
     * The calendar year in which the fiscal year containing the given date ends. Dates on or after
     * 1 July belong to the fiscal year ending the following calendar year; earlier dates belong to
     * the one ending in their own calendar year.
     */
    public static int endingYear(LocalDate date) {
        return date.getMonthValue() >= START.getValue() ? date.getYear() + 1 : date.getYear();
    }

    /**
     * The fiscal year of the given date formatted {@code 'FY<YY>'}, where YY is the last two digits
     * of its {@link #endingYear(LocalDate) ending year} (e.g. {@code 'FY27'}).
     */
    public static String label(LocalDate date) {
        return String.format("FY%02d", endingYear(date) % 100);
    }

    /**
     * The number of whole fiscal years elapsed from {@code from} to {@code to}: the count of 1 July
     * boundaries crossed between the two dates. Two dates in the same fiscal year give 0.
     */
    public static long elapsed(LocalDate from, LocalDate to) {
        return endingYear(to) - endingYear(from);
    }
}
