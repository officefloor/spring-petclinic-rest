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

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Resolves the effective registration date to a business day: a supplied date is used as-is,
 * a missing one defaults to the server's current date, and either way a Saturday or Sunday is
 * rolled forward to the following Monday. Kept as one tiny, self-contained unit so every rule
 * that depends on the registration date shares the same adjustment without growing a handler.
 */
final class RegistrationBusinessDay {

    private RegistrationBusinessDay() {
    }

    static LocalDate effective(LocalDate supplied) {
        LocalDate date = supplied == null ? LocalDate.now() : supplied;
        if (date.getDayOfWeek() == DayOfWeek.SATURDAY) {
            return date.plusDays(2);
        }
        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return date.plusDays(1);
        }
        return date;
    }
}
