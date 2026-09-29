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

package org.springframework.samples.petclinic.rest.validation;

import java.time.LocalDate;

/**
 * Utility for normalizing an owner's registration date to a business day. The effective
 * registration date must never fall on a weekend, so a Saturday or Sunday is rolled forward
 * to the following Monday; a weekday value is left untouched. This applies to the effective
 * date whether it was supplied in the request or defaulted to the server date, and any value
 * derived from the registration date (such as the membership number's year segment) uses the
 * adjusted date.
 */
public final class RegistrationDateNormalizer {

    private RegistrationDateNormalizer() {
    }

    /**
     * Normalize the supplied registration date to a business day.
     *
     * @param date the effective registration date (must not be {@code null})
     * @return the same date when it falls on a weekday, otherwise the following Monday
     */
    public static LocalDate normalize(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case SATURDAY -> date.plusDays(2);
            case SUNDAY -> date.plusDays(1);
            default -> date;
        };
    }
}
