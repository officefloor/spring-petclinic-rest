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
 * The fiscal year a date falls in, where the fiscal year starts on 1 July and is identified
 * by the calendar year it begins in (so 2026-07-01 through 2027-06-30 is fiscal year 2026).
 * A date on or after 1 July belongs to the fiscal year that starts that calendar year;
 * an earlier date belongs to the one that started the previous calendar year.
 */
public final class FiscalYear {

    /** The month the fiscal year starts on: 1 July. */
    public static final Month START_MONTH = Month.JULY;

    private FiscalYear() {
    }

    /**
     * The fiscal year {@code date} falls in, identified by the calendar year it starts in.
     *
     * @param date the date to place in a fiscal year
     * @return the calendar year the containing fiscal year starts in
     */
    public static int startYearOf(LocalDate date) {
        return date.getMonthValue() >= START_MONTH.getValue() ? date.getYear() : date.getYear() - 1;
    }

    /**
     * The {@code FY<YY>} label for the fiscal year {@code date} falls in, where {@code YY} is
     * the last two digits of the fiscal year's start year, e.g. {@code "FY26"}.
     *
     * @param date the date to label
     * @return the fiscal-year label
     */
    public static String labelOf(LocalDate date) {
        return String.format("FY%02d", startYearOf(date) % 100);
    }

    /**
     * The number of whole fiscal years elapsed from {@code from} to {@code to}: the difference
     * between the fiscal years the two dates fall in. Zero when both dates are in the same
     * fiscal year, negative when {@code to} precedes {@code from}.
     *
     * @param from the start date
     * @param to   the end date
     * @return the count of fiscal-year boundaries crossed from {@code from} to {@code to}
     */
    public static int elapsedBetween(LocalDate from, LocalDate to) {
        return startYearOf(to) - startYearOf(from);
    }
}
