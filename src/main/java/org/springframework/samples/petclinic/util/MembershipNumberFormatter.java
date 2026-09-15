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

package org.springframework.samples.petclinic.util;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Builds an owner's membership number from its stored fields.
 */
public abstract class MembershipNumberFormatter {

    /**
     * Builds an owner's membership number from its stored fields, formatted
     * {@code '<customerCode>-M<YY>'} where {@code YY} is the last two digits of the
     * {@link FiscalYearResolver#yearOf(java.time.LocalDate) fiscal year} of the
     * (business-day-adjusted) registration date, e.g. {@code "NSW-3F2A9C1E-M27"}. Returns
     * {@code null} when either the customer code or registration date is absent.
     */
    public static String membershipNumber(Owner owner) {
        if (owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("%s-M%02d", owner.getCustomerCode(),
            FiscalYearResolver.yearOf(owner.getRegistrationDate()) % 100);
    }

}
