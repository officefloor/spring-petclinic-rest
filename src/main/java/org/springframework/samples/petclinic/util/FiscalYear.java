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
 * Utilities for the business fiscal year, which starts on 1 July. A fiscal year is identified by
 * the calendar year in which it begins: every date from 1 July of year {@code Y} up to 30 June of
 * year {@code Y + 1} belongs to fiscal year {@code Y}.
 */
public final class FiscalYear {

    /** The month on whose first day each fiscal year begins. */
    private static final Month FISCAL_YEAR_START = Month.JULY;

    private FiscalYear() {
    }

    /**
     * Return the calendar year in which the fiscal year containing {@code date} began: the date's
     * own year from 1 July onwards, otherwise the previous calendar year.
     *
     * @param date the date whose fiscal year to resolve
     * @return the fiscal year's starting calendar year
     */
    public static int startingYear(LocalDate date) {
        int year = date.getYear();
        return date.getMonthValue() >= FISCAL_YEAR_START.getValue() ? year : year - 1;
    }

    /**
     * Return the last two digits of the fiscal year's starting calendar year, zero-padded (for
     * example {@code "26"} for fiscal year 2026).
     *
     * @param date the date whose fiscal year to format, may be {@code null}
     * @return the two-digit fiscal year, or {@code null} if {@code date} is {@code null}
     */
    public static String twoDigit(LocalDate date) {
        if (date == null) {
            return null;
        }
        return String.format("%02d", startingYear(date) % 100);
    }

    /**
     * Format the fiscal year containing {@code date} as {@code "FY<YY>"}, where {@code YY} is the
     * last two digits of the fiscal year's starting calendar year, zero-padded (for example
     * {@code "FY26"}).
     *
     * @param date the date whose fiscal year to format, may be {@code null}
     * @return the formatted fiscal year, or {@code null} if {@code date} is {@code null}
     */
    public static String label(LocalDate date) {
        if (date == null) {
            return null;
        }
        return "FY" + twoDigit(date);
    }

    /**
     * Return the number of whole fiscal years elapsed from {@code from} to {@code to}: the count of
     * 1 July boundaries crossed between the two dates. Never negative.
     *
     * @param from the earlier date
     * @param to   the later date
     * @return the number of elapsed fiscal years, never negative
     */
    public static int elapsed(LocalDate from, LocalDate to) {
        return Math.max(startingYear(to) - startingYear(from), 0);
    }
}
