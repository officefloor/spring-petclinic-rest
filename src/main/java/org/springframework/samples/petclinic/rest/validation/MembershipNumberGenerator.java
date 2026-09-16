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
 * Derives an owner's membership number, formatted {@code <customerCode>-M<YY>} where YY is
 * the last two digits, zero-padded, of the registration date year
 * (e.g. {@code SMI-0007-M26}).
 */
public final class MembershipNumberGenerator {

    private MembershipNumberGenerator() {
    }

    /**
     * Builds the membership number for a new owner.
     *
     * @param customerCode     the owner's customer code
     * @param registrationDate the owner's registration date
     * @return the membership number, e.g. {@code "SMI-0007-M26"}
     */
    public static String generate(String customerCode, LocalDate registrationDate) {
        return String.format("%s-M%02d", customerCode, registrationDate.getYear() % 100);
    }
}
