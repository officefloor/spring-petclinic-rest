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

import org.springframework.samples.petclinic.model.Owner;

/**
 * The single source of truth for an owner's membership number, formatted
 * '&lt;customerCode&gt;-M&lt;YY&gt;' where YY is the last two digits of the fiscal year of the
 * registrationDate. Both the API projection and the audit record derive it through here so
 * the format lives in exactly one place.
 */
public final class MembershipNumber {

    private MembershipNumber() {
    }

    public static String of(Owner owner) {
        return owner.getCustomerCode() + "-M"
            + FiscalYear.label(owner.getRegistrationDate()).substring(2);
    }
}
