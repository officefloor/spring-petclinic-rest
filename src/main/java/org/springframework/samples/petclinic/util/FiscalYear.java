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
package org.springframework.samples.petclinic.util;

import java.time.LocalDate;
import java.time.Month;

/**
 * The fiscal-year calendar used for date-derived owner values.
 *
 * <p>A fiscal year begins on 1 July and is numbered by the calendar year in which
 * it ends, so 1 July 2026 through 30 June 2027 is fiscal year 2027. This type owns
 * that convention: it maps a date to the fiscal year it falls in, formats the
 * {@code 'FY<YY>'} label used by identifiers, and counts whole fiscal years elapsed
 * between two dates.
 */
public final class FiscalYear {

    /** The month on whose first day each fiscal year begins. */
    public static final Month START_MONTH = Month.JULY;

    private FiscalYear() {
    }

    /**
     * The fiscal year that {@code date} falls in, numbered by the calendar year in
     * which it ends (e.g. any date from 1 July 2026 to 30 June 2027 is {@code 2027}).
     *
     * @param date the date to classify
     * @return the four-digit fiscal year
     */
    public static int of(LocalDate date) {
        return date.getMonthValue() >= START_MONTH.getValue() ? date.getYear() + 1 : date.getYear();
    }

    /**
     * The two-digit year segment of the fiscal year that {@code date} falls in, for
     * use in identifiers (e.g. {@code 7} for fiscal year 2027).
     *
     * @param date the date to classify
     * @return the fiscal year modulo 100
     */
    public static int yearSegment(LocalDate date) {
        return of(date) % 100;
    }

    /**
     * The fiscal-year label {@code 'FY<YY>'} for {@code date} (e.g. {@code "FY27"}).
     *
     * @param date the date to classify, or {@code null} when unknown
     * @return the label, or {@code null} when {@code date} is {@code null}
     */
    public static String label(LocalDate date) {
        if (date == null) {
            return null;
        }
        return String.format("FY%02d", yearSegment(date));
    }

    /**
     * The number of whole fiscal years elapsed from {@code start} to {@code end}: the
     * count of 1 July boundaries crossed between them, i.e. {@code of(end) - of(start)}.
     * Zero when both dates fall in the same fiscal year, and never negative when
     * {@code end} is not before {@code start}.
     *
     * @param start the earlier date
     * @param end   the later date
     * @return the number of elapsed fiscal years
     */
    public static long elapsedYears(LocalDate start, LocalDate end) {
        return (long) of(end) - of(start);
    }
}
