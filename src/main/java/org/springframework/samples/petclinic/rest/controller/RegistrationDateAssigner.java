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

package org.springframework.samples.petclinic.rest.controller;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

/**
 * Assigns an owner's effective registration date on create.
 * <p>
 * The effective date is the one supplied in the request, or the server's current date when
 * none was supplied so every stored owner carries the day it was registered. The effective
 * date must fall on a business day: a Saturday or Sunday is rolled forward to the following
 * Monday. Everything derived from the registration date (such as the membership number's year
 * segment) therefore uses this adjusted business-day value.
 */
@Component
public class RegistrationDateAssigner {

    private final Clock clock;

    private final BusinessDayAdjuster businessDayAdjuster;

    public RegistrationDateAssigner(Clock clock, BusinessDayAdjuster businessDayAdjuster) {
        this.clock = clock;
        this.businessDayAdjuster = businessDayAdjuster;
    }

    /**
     * Sets {@code owner}'s registration date to its effective value, defaulting to the server's
     * current date when none was supplied and rolling any weekend date forward to the next
     * business day.
     *
     * @param owner the owner being created
     */
    public void assign(Owner owner) {
        LocalDate effectiveDate = owner.getRegistrationDate() != null
            ? owner.getRegistrationDate()
            : LocalDate.now(clock);
        owner.setRegistrationDate(businessDayAdjuster.toBusinessDay(effectiveDate));
    }
}
