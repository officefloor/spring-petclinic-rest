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

/**
 * Fiscal-year arithmetic for a year that starts on 1 July. The fiscal year is named
 * for the calendar year it ends in, so July–December fall in the following year's
 * fiscal year and its {@code label} is {@code FY<YY>}, e.g. {@code FY26}.
 */
public final class FiscalYear {

    private FiscalYear() {
    }

    /** The fiscal year that contains {@code date}. */
    public static int of(LocalDate date) {
        return date.getMonthValue() >= 7 ? date.getYear() + 1 : date.getYear();
    }

    /** {@code FY<YY>} for the fiscal year of the business-day-adjusted {@code date},
     *  or {@code null} when {@code date} is null. */
    public static String label(LocalDate date) {
        return date == null ? null : String.format("FY%02d", of(BusinessDay.adjust(date)) % 100);
    }

    /** Whole fiscal years elapsed from {@code date} to {@code asOf}. */
    public static int elapsed(LocalDate date, LocalDate asOf) {
        return of(asOf) - of(date);
    }
}
