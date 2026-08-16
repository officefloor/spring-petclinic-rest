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

package org.springframework.samples.petclinic.rest.controller;

/**
 * Raised when creating an owner whose whole derived {@code identityKey}
 * ({@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}) already equals
 * that of an existing owner. This single key consolidates the former separate telephone,
 * email and household duplicate checks. Carries the offending key so the exception handler
 * can report it to the client as a 409 Conflict.
 */
public class DuplicateIdentityException extends RuntimeException {

    private final String identityKey;

    public DuplicateIdentityException(String identityKey) {
        super("An owner with identity key '" + identityKey + "' already exists");
        this.identityKey = identityKey;
    }

    public String getIdentityKey() {
        return this.identityKey;
    }
}
