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

/**
 * Formats an owner's membership number from the owner's own fields.
 *
 * <p>The number is formatted '&lt;customerCode&gt;-M&lt;YY&gt;' where YY is the last
 * two digits of the {@link FiscalYear fiscal year} of the registrationDate
 * (e.g. 'NSW-1A2B3C4D-M26'). It is {@code null} until both the customer code and
 * registration date are assigned.
 */
public final class MembershipNumber {

    private MembershipNumber() {
    }

    /**
     * Derive the membership number for the given owner.
     *
     * @param owner the owner.
     * @return the membership number, or {@code null} when the owner, its customer
     *         code or its registration date is absent.
     */
    public static String of(Owner owner) {
        if (owner == null || owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return owner.getCustomerCode() + "-M" + FiscalYear.twoDigitYear(owner.getRegistrationDate());
    }
}
