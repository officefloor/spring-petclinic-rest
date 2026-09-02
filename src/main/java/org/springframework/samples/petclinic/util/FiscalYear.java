/*
 * Copyright 2016 the original author or authors.
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
 * Fiscal-year arithmetic: the fiscal year runs 1 July to 30 June and is named by the calendar year
 * in which it ends, so a July date belongs to the following year's fiscal year.
 */
public final class FiscalYear {

    private FiscalYear() {
    }

    /** The fiscal year a date falls in (e.g. 2027 for 2 July 2026). */
    public static int of(LocalDate date) {
        return date.getMonthValue() >= Month.JULY.getValue() ? date.getYear() + 1 : date.getYear();
    }

    /** The fiscal year of a date formatted {@code 'FY<YY>'} (last two digits). */
    public static String label(LocalDate date) {
        return String.format("FY%02d", of(date) % 100);
    }

    /** Whole fiscal years elapsed from one date to another. */
    public static int elapsed(LocalDate from, LocalDate to) {
        return of(to) - of(from);
    }
}
