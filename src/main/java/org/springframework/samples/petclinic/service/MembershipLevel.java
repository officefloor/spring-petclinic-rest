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
import java.time.temporal.ChronoUnit;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's membership level. The pre-tenure factors (an email and a zero
 * namesakeCount) each add a point above the base of 1 and cap at level 3. Level 4 is
 * reserved for loyalty: it additionally requires tenure of more than 365 days since
 * registration, so a newly created owner (zero tenure) never exceeds level 3.
 */
public final class MembershipLevel {

    private MembershipLevel() {
    }

    public static int of(Owner owner) {
        int base = 1
            + (owner.getEmail() != null && !owner.getEmail().isBlank() ? 1 : 0)
            + (Integer.valueOf(0).equals(owner.getNamesakeCount()) ? 1 : 0);
        boolean tenured = ChronoUnit.DAYS.between(owner.getRegistrationDate(), LocalDate.now()) > 365;
        return Math.min(tenured ? 4 : 3, base + (tenured ? 1 : 0));
    }
}
