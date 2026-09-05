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

import org.springframework.samples.petclinic.model.Owner;

/**
 * Scores an owner's membership from a base of 0: +2 for a present email, +1 when
 * namesakeCount is 0, +2 for a household of 3 or more, and +3 for tenure over one
 * elapsed fiscal year since registration.
 */
public final class MembershipPoints {

    private MembershipPoints() {
    }

    public static int of(Owner owner) {
        int points = 0;
        points += owner.getEmail() != null && !owner.getEmail().isBlank() ? 2 : 0;
        points += Integer.valueOf(0).equals(owner.getNamesakeCount()) ? 1 : 0;
        points += owner.getHouseholdSize() != null && owner.getHouseholdSize() >= 3 ? 2 : 0;
        points += FiscalYear.elapsed(owner.getRegistrationDate(), LocalDate.now()) > 1 ? 3 : 0;
        return points;
    }
}
