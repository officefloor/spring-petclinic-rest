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
 * A fiscal year that runs from 1 July to the following 30 June, identified by the calendar
 * year in which it starts. A date on or after 1 July belongs to the fiscal year starting
 * that same calendar year; an earlier date belongs to the one that started the previous
 * year. The fiscal year is labelled {@code "FY<YY>"} using the last two digits of its
 * starting calendar year (e.g. 1 July 2026 to 30 June 2027 is {@code "FY26"}).
 */
public final class FiscalYear {

    /** The month on whose first day each fiscal year begins. */
    private static final Month START_MONTH = Month.JULY;

    private final int startYear;

    private FiscalYear(int startYear) {
        this.startYear = startYear;
    }

    /**
     * Returns the fiscal year that contains {@code date}.
     */
    public static FiscalYear containing(LocalDate date) {
        int startYear = date.getMonthValue() >= START_MONTH.getValue()
            ? date.getYear() : date.getYear() - 1;
        return new FiscalYear(startYear);
    }

    /**
     * The calendar year in which this fiscal year begins.
     */
    public int startYear() {
        return this.startYear;
    }

    /**
     * The last two digits of this fiscal year's starting calendar year, for composing
     * fiscal-year-based identifiers.
     */
    public int shortYear() {
        return Math.floorMod(this.startYear, 100);
    }

    /**
     * The number of whole fiscal years elapsed from this fiscal year to {@code other}, i.e.
     * the count of fiscal-year boundaries between them. Negative when {@code other} precedes
     * this fiscal year.
     */
    public int yearsUntil(FiscalYear other) {
        return other.startYear - this.startYear;
    }

    /**
     * This fiscal year's label, formatted {@code "FY<YY>"}.
     */
    public String label() {
        return String.format("FY%02d", shortYear());
    }
}
