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

package org.springframework.samples.petclinic.model;

import java.time.LocalDate;
import java.time.Month;

/**
 * The fiscal year, which starts on 1 July and is labelled by the calendar year in which it ends
 * (the Australian convention): the fiscal year running from 1 July 2025 to 30 June 2026 is
 * {@code FY26}. Provides the label for a date and the whole number of fiscal years elapsed
 * between two dates.
 */
public final class FiscalYear {

    /** The month on whose first day each fiscal year begins. */
    private static final Month FISCAL_YEAR_START = Month.JULY;

    private FiscalYear() {
    }

    /**
     * The fiscal year that contains the given date, as the calendar year in which it ends.
     *
     * @param date the date to classify
     * @return the ending calendar year of the fiscal year (e.g. 2026 for a date in FY26)
     */
    public static int of(LocalDate date) {
        boolean afterStart = date.getMonthValue() >= FISCAL_YEAR_START.getValue();
        return afterStart ? date.getYear() + 1 : date.getYear();
    }

    /**
     * The fiscal year of the given date formatted as {@code FY<YY>}, where YY is the last two
     * digits, zero-padded, of the fiscal year (e.g. {@code FY26}).
     *
     * @param date the date to classify
     * @return the fiscal year label
     */
    public static String labelOf(LocalDate date) {
        return String.format("FY%02d", of(date) % 100);
    }

    /**
     * The whole number of fiscal years elapsed from one date to another: the number of 1 July
     * fiscal-year boundaries crossed between them.
     *
     * @param from the earlier date
     * @param to   the later date
     * @return the count of fiscal years elapsed (zero when both fall in the same fiscal year)
     */
    public static int elapsedBetween(LocalDate from, LocalDate to) {
        return of(to) - of(from);
    }
}
