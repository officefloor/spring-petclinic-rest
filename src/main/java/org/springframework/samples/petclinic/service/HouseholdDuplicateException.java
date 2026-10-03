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

/**
 * Thrown when an attempt is made to create an {@code Owner} that falls into an existing household —
 * i.e. another owner already shares its last name and postcode (so they share the deterministic
 * {@code householdId}) — without declaring, via the {@code sharesHousehold} request flag, that it
 * genuinely shares the household. Setting {@code sharesHousehold} bypasses this block and the owner
 * is created as a declared household member. Enforcing it in the service layer keeps the invariant in
 * one place regardless of which caller initiates the create; the REST layer maps this to a
 * {@code 409 Conflict}.
 */
public class HouseholdDuplicateException extends RuntimeException {

    public HouseholdDuplicateException(String householdId) {
        super("An owner already exists in household '" + householdId
            + "'; set sharesHousehold to create a declared household member");
    }

}
