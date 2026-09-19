/*
 * Copyright 2002-2017 the original author or authors.
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

import org.springframework.stereotype.Component;

/**
 * Builds an owner's {@code membershipNumber}, formatted {@code <customerCode>-M<YY>}
 * where {@code YY} is the last two digits of the registration date's year
 * (e.g. {@code SMI-0007-M26}).
 */
@Component
public class MembershipNumberGenerator {

    /**
     * Build the membership number for an owner.
     *
     * @param customerCode     the owner's customer code, used as the prefix
     * @param registrationDate the owner's registration date; its year supplies the two-digit suffix
     * @return the formatted membership number, e.g. {@code SMI-0007-M26}
     */
    public String generate(String customerCode, LocalDate registrationDate) {
        return String.format("%s-M%02d", customerCode, registrationDate.getYear() % 100);
    }
}
