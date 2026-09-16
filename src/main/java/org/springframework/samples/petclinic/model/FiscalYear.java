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
 * The fiscal year a date falls in. The fiscal year starts on 1 July and is identified by
 * the calendar year in which it begins, so a date on or after 1 July belongs to that
 * year's fiscal year and an earlier date belongs to the previous one. This is the single
 * source of truth for turning a date into a fiscal year, so callers never repeat it.
 */
public final class FiscalYear {

    /** The month a fiscal year starts on: 1 July. */
    static final Month START_MONTH = Month.JULY;

    private FiscalYear() {
    }

    /**
     * The calendar year in which the fiscal year containing {@code date} begins.
     */
    public static int startingYearOf(LocalDate date) {
        return date.getMonthValue() >= START_MONTH.getValue() ? date.getYear() : date.getYear() - 1;
    }

    /**
     * The last two digits of the fiscal year's starting calendar year, used as the year
     * segment of date-derived identifiers.
     */
    public static int shortYearOf(LocalDate date) {
        return startingYearOf(date) % 100;
    }

    /**
     * The fiscal-year label for {@code date}, formatted {@code 'FY<YY>'} where YY is the
     * last two digits of the fiscal year's starting calendar year (e.g. {@code "FY26"}).
     */
    public static String labelFor(LocalDate date) {
        return String.format("FY%02d", shortYearOf(date));
    }

    /**
     * The number of whole fiscal years elapsed from {@code from} to {@code to}, i.e. how
     * many 1-July boundaries the interval crosses.
     */
    public static int elapsedBetween(LocalDate from, LocalDate to) {
        return startingYearOf(to) - startingYearOf(from);
    }
}
