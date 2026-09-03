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
 * Places dates on a fiscal-year basis, where the fiscal year starts on 1 July.
 */
public final class FiscalYears {

    private FiscalYears() {
    }

    /** The calendar year in which the fiscal year containing {@code date} began. */
    public static int startYear(LocalDate date) {
        return date.getMonthValue() >= Month.JULY.getValue() ? date.getYear() : date.getYear() - 1;
    }

    /** The {@code FY<YY>} label of {@code date}, where YY is the last two digits of its fiscal year. */
    public static String label(LocalDate date) {
        return String.format("FY%02d", startYear(date) % 100);
    }

    /** Whole fiscal years elapsed from {@code from} to {@code to}. */
    public static int elapsed(LocalDate from, LocalDate to) {
        return startYear(to) - startYear(from);
    }
}
