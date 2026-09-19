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

/**
 * Thrown when an owner is created into a household that already has a member, i.e. another owner
 * already shares the same {@code householdId} (the same last name and postcode), without declaring
 * {@code sharesHousehold}. Signals a conflict with existing data rather than a bad request.
 */
public class HouseholdDuplicateException extends RuntimeException {

    public HouseholdDuplicateException(String householdId) {
        super("An owner already belongs to household '" + householdId + "'");
    }
}
