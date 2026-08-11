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
 * Thrown when a request to create an owner produces an identity key
 * ({@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}) whose whole value is
 * already used by another owner. This single check consolidates the former separate telephone,
 * email and household duplicate checks. Carries the offending identity key so the exception handler
 * can report it.
 */
public class DuplicateIdentityException extends RuntimeException {

    private final String rejectedValue;

    public DuplicateIdentityException(String rejectedValue) {
        super("An owner with the same identity key already exists");
        this.rejectedValue = rejectedValue;
    }

    public String getRejectedValue() {
        return this.rejectedValue;
    }
}
