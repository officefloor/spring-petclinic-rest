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
 * Builds an owner's {@code membershipNumber}, formatted {@code <customerCode>-M<YY>}:
 * the customer code followed by the last two digits of the registration date's
 * fiscal year (starting 1 July), e.g. {@code SMI-0007-M26}.
 */
public final class MembershipNumber {

    private MembershipNumber() {
    }

    /**
     * @param customerCode     the owner's customer code
     * @param registrationDate the owner's registration date
     * @return the formatted membership number, or {@code null} if either input is null
     */
    public static String of(String customerCode, LocalDate registrationDate) {
        if (customerCode == null || registrationDate == null) {
            return null;
        }
        return String.format("%s-M%02d", customerCode, FiscalYear.of(registrationDate) % 100);
    }
}
