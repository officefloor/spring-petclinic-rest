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
 * Formats an owner's membership number as {@code "<customerCode>-M<YY>"}, where
 * {@code YY} is the last two digits of the registration date's year
 * (e.g. {@code "NSW-A1B2C3D4-M26"}). The number is undefined until both the customer code
 * and registration date have been assigned.
 */
public final class MembershipNumber {

    private MembershipNumber() {
    }

    /**
     * Returns the formatted membership number for the owner, or {@code null} when the owner
     * is {@code null} or its customer code or registration date has not been assigned yet.
     */
    public static String forOwner(Owner owner) {
        if (owner == null || owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("%s-M%02d", owner.getCustomerCode(), owner.getRegistrationDate().getYear() % 100);
    }
}
