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
package org.springframework.samples.petclinic.service;

import java.time.LocalDate;

/**
 * Fiscal-year helpers where the fiscal year starts on 1 July and is numbered by the
 * calendar year it ends in: a date on or after 1 July belongs to the next year's fiscal
 * year (e.g. 2025-09-01 is FY26).
 */
public final class FiscalYear {

    private FiscalYear() {
    }

    /** The fiscal year a date falls in, numbered by the year the fiscal year ends. */
    public static int yearOf(LocalDate date) {
        return date.getMonthValue() >= 7 ? date.getYear() + 1 : date.getYear();
    }

    /** The fiscal year label 'FY&lt;YY&gt;' for a date, e.g. 'FY26'. */
    public static String label(LocalDate date) {
        return String.format("FY%02d", yearOf(date) % 100);
    }

    /** Whole fiscal years elapsed from {@code from} to {@code to}. */
    public static int elapsed(LocalDate from, LocalDate to) {
        return yearOf(to) - yearOf(from);
    }
}
