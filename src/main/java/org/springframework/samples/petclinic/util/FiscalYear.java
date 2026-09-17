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
 * Utility for deriving fiscal-year values. The fiscal year starts on 1 July and is identified by
 * the calendar year in which it ends, so 1 July 2020 to 30 June 2021 is fiscal year 2021.
 */
public final class FiscalYear {

    /** The month on whose first day each fiscal year starts. */
    private static final Month FISCAL_YEAR_START = Month.JULY;

    private FiscalYear() {
    }

    /**
     * The fiscal year containing {@code date}, identified by the calendar year in which it ends:
     * dates on or after 1 July belong to the fiscal year ending the following calendar year, all
     * earlier dates to the fiscal year ending in their own calendar year.
     *
     * @param date the date to classify
     * @return the fiscal year, as its ending calendar year
     */
    public static int of(LocalDate date) {
        return date.getMonthValue() >= FISCAL_YEAR_START.getValue() ? date.getYear() + 1 : date.getYear();
    }

    /**
     * The fiscal-year label {@code "FY<YY>"} for the fiscal year containing {@code date}, where YY
     * is the last two digits of the {@linkplain #of(LocalDate) fiscal year}, e.g. {@code "FY21"}.
     *
     * @param date the date to label
     * @return the fiscal-year label
     */
    public static String label(LocalDate date) {
        return String.format("FY%02d", of(date) % 100);
    }

    /**
     * The number of fiscal years elapsed from {@code start} to {@code end}: the difference between
     * their {@linkplain #of(LocalDate) fiscal years}, i.e. the number of 1 July boundaries crossed.
     *
     * @param start the earlier date
     * @param end   the later date
     * @return the elapsed fiscal years (zero when both dates fall in the same fiscal year)
     */
    public static int yearsBetween(LocalDate start, LocalDate end) {
        return of(end) - of(start);
    }
}
