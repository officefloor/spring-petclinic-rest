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

package org.springframework.samples.petclinic.rest.advice;

import java.time.LocalDate;
import java.time.Month;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Fiscal-year helpers for an owner's business-day-adjusted registration date, where the fiscal
 * year starts on 1 July, so a July-to-December date belongs to the next calendar year's fiscal
 * year. Kept as one tiny, self-contained unit so the membership number, the exposed fiscalYear
 * label and the tenure rule all share the same basis without growing a handler.
 */
public final class OwnerFiscalYear {

    private OwnerFiscalYear() {
    }

    /** Fiscal year of {@code date}: the calendar year, rolled to the next year from 1 July onward. */
    public static int yearOf(LocalDate date) {
        return date.getMonthValue() >= Month.JULY.getValue() ? date.getYear() + 1 : date.getYear();
    }

    /** {@code FY<YY>} for the owner's registration date, or {@code null} when it is absent. */
    public static String label(Owner owner) {
        LocalDate registered = owner.getRegistrationDate();
        return registered == null ? null : String.format("FY%02d", yearOf(registered) % 100);
    }

    /** Whole fiscal years elapsed between the owner's registration date and today (0 when absent). */
    static int elapsed(Owner owner) {
        LocalDate registered = owner.getRegistrationDate();
        return registered == null ? 0 : yearOf(LocalDate.now()) - yearOf(registered);
    }
}
