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
 * Derivation of the fiscal year a date falls in. Kept separate from the owner controller and the
 * {@code Owner} model so the rule lives in one place, as a pure function with no web or persistence
 * dependencies. Mirrors {@link BusinessDayAdjuster}, whose business-day-adjusted date this is
 * derived from.
 *
 * <p>The fiscal year starts on 1 July and is identified by the calendar year in which it ends: a
 * date on or after 1 July belongs to the fiscal year of {@code year + 1}, and a date before 1 July
 * belongs to the fiscal year of {@code year}. So 2026-09-04 falls in fiscal year 2027 and 2026-03-04
 * falls in fiscal year 2026. The {@linkplain #label label} form is {@code FY} followed by the last
 * two digits of that year, e.g. {@code FY27}.
 */
public abstract class FiscalYear {

    /**
     * The fiscal year (as a full calendar year) that {@code date} falls in. The fiscal year starts
     * on 1 July and is named for the calendar year in which it ends.
     *
     * @param date the date (must not be null)
     * @return the fiscal year, e.g. {@code 2027} for 2026-09-04
     */
    public static int yearOf(LocalDate date) {
        return date.getMonthValue() >= Month.JULY.getValue() ? date.getYear() + 1 : date.getYear();
    }

    /**
     * The {@code FY<YY>} label of the fiscal year {@code date} falls in, where {@code YY} is the last
     * two digits of the fiscal year, zero-padded.
     *
     * @param date the date (must not be null)
     * @return the fiscal year label, e.g. {@code FY27} for 2026-09-04
     */
    public static String label(LocalDate date) {
        return String.format("FY%02d", yearOf(date) % 100);
    }

    /**
     * The number of fiscal years elapsed between two dates, i.e. the number of 1-July boundaries
     * crossed from {@code from} to {@code to}.
     *
     * @param from the earlier date (must not be null)
     * @param to   the later date (must not be null)
     * @return {@code yearOf(to) - yearOf(from)}
     */
    public static int elapsed(LocalDate from, LocalDate to) {
        return yearOf(to) - yearOf(from);
    }

}
