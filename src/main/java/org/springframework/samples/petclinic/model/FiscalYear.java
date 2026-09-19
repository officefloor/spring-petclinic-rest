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
 * The fiscal year a date falls in. Each fiscal year runs from 1 July to the following
 * 30 June and is named by the calendar year in which it ends, so 1 July 2025 through
 * 30 June 2026 is fiscal year 2026.
 */
public final class FiscalYear {

    /** The month on whose first day each fiscal year begins. */
    private static final Month FISCAL_YEAR_START = Month.JULY;

    private final int year;

    private FiscalYear(int year) {
        this.year = year;
    }

    /**
     * The fiscal year that {@code date} falls in: the date's calendar year on or after
     * 1 July, otherwise the previous calendar year (i.e. the year the fiscal year ends).
     */
    public static FiscalYear of(LocalDate date) {
        int calendarYear = date.getYear();
        int fiscalYear = date.getMonthValue() >= FISCAL_YEAR_START.getValue() ? calendarYear + 1 : calendarYear;
        return new FiscalYear(fiscalYear);
    }

    /** The full fiscal year number, e.g. {@code 2026}. */
    public int getYear() {
        return this.year;
    }

    /** The last two digits of the fiscal year, e.g. {@code 26}. */
    public int getShortYear() {
        return this.year % 100;
    }

    /** The fiscal year formatted {@code FY<YY>} from its {@linkplain #getShortYear() two-digit year}, e.g. {@code FY26}. */
    public String getLabel() {
        return String.format("FY%02d", getShortYear());
    }

    /** The number of whole fiscal years from this fiscal year up to {@code other} (0 within the same fiscal year). */
    public int yearsUntil(FiscalYear other) {
        return other.year - this.year;
    }
}
