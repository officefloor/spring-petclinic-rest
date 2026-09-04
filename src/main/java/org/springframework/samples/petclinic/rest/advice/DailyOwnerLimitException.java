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
import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Signals that the maximum number of owners registrable in a single day has been reached.
 * Mapped to HTTP 429 Too Many Requests by {@link ExceptionControllerAdvice}.
 */
public class DailyOwnerLimitException extends RuntimeException {

    /** Maximum owners registrable per day; once this many share today's date the next is rejected. */
    private static final int DAILY_LIMIT = 100;

    public DailyOwnerLimitException() {
        super("Daily owner registration limit reached: " + DAILY_LIMIT);
    }

    /**
     * Throw if {@code existingOwners} already contains {@value #DAILY_LIMIT} or more owners
     * whose registration date is today.
     */
    public static void rejectIfAtLimit(Collection<Owner> existingOwners) {
        LocalDate today = LocalDate.now();
        long count = existingOwners.stream()
            .filter(owner -> today.equals(owner.getRegistrationDate()))
            .count();
        if (count >= DAILY_LIMIT) {
            throw new DailyOwnerLimitException();
        }
    }
}
