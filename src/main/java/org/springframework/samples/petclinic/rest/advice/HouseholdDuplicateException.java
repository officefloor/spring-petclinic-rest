/*
 * Copyright 2016 the original author or authors.
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

package org.springframework.samples.petclinic.rest.advice;

/**
 * Thrown when a submitted owner joins an existing household — it derives the same deterministic
 * {@code householdId} (from its normalized lastName and postcode) as an existing owner — without
 * declaring {@code sharesHousehold=true}. Carries the offending householdId so it can be reported
 * to the client. A create that does declare {@code sharesHousehold=true} is allowed through as a
 * declared household member and does not raise this exception.
 */
public class HouseholdDuplicateException extends RuntimeException {

    private final String rejectedValue;

    public HouseholdDuplicateException(String rejectedValue) {
        super("Owner joins an existing household without sharesHousehold=true: " + rejectedValue);
        this.rejectedValue = rejectedValue;
    }

    public String getRejectedValue() {
        return this.rejectedValue;
    }
}
