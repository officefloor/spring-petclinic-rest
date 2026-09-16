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
import java.time.temporal.ChronoUnit;

/**
 * How long an owner has been registered, measured in whole days from their
 * registration date. Owns the membership tenure boundary so it lives in one
 * place rather than being scattered across callers.
 */
public final class Tenure {

    /** An owner must exceed this many days of tenure to qualify for the top membership level. */
    static final long QUALIFYING_DAYS = 365;

    private Tenure() {
    }

    /**
     * The owner's tenure in whole days, from {@code registrationDate} up to {@code asOf}.
     */
    public static long daysAsOf(LocalDate registrationDate, LocalDate asOf) {
        return ChronoUnit.DAYS.between(registrationDate, asOf);
    }

    /**
     * Whether an owner registered on {@code registrationDate} has, as of {@code asOf},
     * accrued enough tenure (more than {@link #QUALIFYING_DAYS} days) to qualify for the
     * top membership level.
     */
    public static boolean qualifiesForTopLevel(LocalDate registrationDate, LocalDate asOf) {
        return daysAsOf(registrationDate, asOf) > QUALIFYING_DAYS;
    }
}
