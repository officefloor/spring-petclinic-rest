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
import java.time.Month;

/**
 * Helpers for placing a date onto a fiscal-year basis. The fiscal year starts on
 * {@value #FISCAL_YEAR_START_MONTH} (1 July) and is named by the calendar year in which it ends, so
 * the fiscal year running 1 July 2025 to 30 June 2026 is fiscal year 2026 (labelled {@code FY26}).
 */
public final class FiscalYears {

    /**
     * The month (1 July) on which each fiscal year starts.
     */
    public static final int FISCAL_YEAR_START_MONTH = Month.JULY.getValue();

    private FiscalYears() {
    }

    /**
     * The numeric fiscal year a date falls in, named by the calendar year in which the fiscal year
     * ends. A date on or after 1 July belongs to the next calendar year's fiscal year; a date before
     * 1 July belongs to the current calendar year's fiscal year.
     *
     * @param date the date to place onto a fiscal year
     * @return the numeric fiscal year (e.g. {@code 2026} for any date in July 2025 to June 2026)
     */
    public static int fiscalYear(LocalDate date) {
        return date.getMonthValue() >= FISCAL_YEAR_START_MONTH ? date.getYear() + 1 : date.getYear();
    }

    /**
     * The fiscal-year label of a date, formatted {@code 'FY<YY>'} where {@code YY} is the last two
     * digits of the {@link #fiscalYear(LocalDate) numeric fiscal year} (e.g. {@code 'FY26'}).
     *
     * @param date the date to place onto a fiscal year, or {@code null} when unknown
     * @return the fiscal-year label, or {@code null} when {@code date} is {@code null}
     */
    public static String fiscalYearLabel(LocalDate date) {
        if (date == null) {
            return null;
        }
        return String.format("FY%02d", fiscalYear(date) % 100);
    }

    /**
     * The number of fiscal years elapsed between two dates, i.e. the count of 1 July boundaries
     * crossed from {@code from} to {@code to}. Equal to the difference of the two dates'
     * {@link #fiscalYear(LocalDate) numeric fiscal years}; zero when both fall in the same fiscal
     * year and negative when {@code to} precedes {@code from}'s fiscal year.
     *
     * @param from the earlier date
     * @param to   the later date
     * @return the number of fiscal years elapsed from {@code from} to {@code to}
     */
    public static int elapsedFiscalYears(LocalDate from, LocalDate to) {
        return fiscalYear(to) - fiscalYear(from);
    }
}
