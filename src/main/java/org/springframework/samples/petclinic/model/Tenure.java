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
 * How long an owner has been registered, measured in whole fiscal years elapsed since
 * their registration date (see {@link FiscalYear}). Owns the membership tenure boundary
 * so it lives in one place rather than being scattered across callers.
 */
public final class Tenure {

    /**
     * An owner must exceed this many elapsed fiscal years of tenure to qualify for the top
     * membership level.
     */
    static final int QUALIFYING_FISCAL_YEARS = 1;

    private Tenure() {
    }

    /**
     * The owner's tenure in whole fiscal years, from {@code registrationDate} up to
     * {@code asOf}.
     */
    public static int fiscalYearsAsOf(LocalDate registrationDate, LocalDate asOf) {
        return FiscalYear.elapsedBetween(registrationDate, asOf);
    }

    /**
     * Whether an owner registered on {@code registrationDate} has, as of {@code asOf},
     * accrued enough tenure (more than {@link #QUALIFYING_FISCAL_YEARS} fiscal year) to
     * qualify for the top membership level.
     */
    public static boolean qualifiesForTopLevel(LocalDate registrationDate, LocalDate asOf) {
        return fiscalYearsAsOf(registrationDate, asOf) > QUALIFYING_FISCAL_YEARS;
    }
}
