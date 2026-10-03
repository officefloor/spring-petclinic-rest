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
 * Thrown when an attempt is made to create an {@code Owner} whose derived {@code identityKey}
 * ({@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}) exactly equals that of
 * an existing owner. This single key consolidates what used to be the separate telephone, email and
 * household duplicate checks: because the telephone is part of the key, two members of the same
 * household with different telephones have different keys and are both allowed; only an exact
 * whole-key match is a duplicate. Enforcing it in the service layer keeps the invariant in one place
 * regardless of which caller initiates the create; the REST layer maps this to a {@code 409 Conflict}.
 */
public class DuplicateIdentityException extends RuntimeException {

    public DuplicateIdentityException(String identityKey) {
        super("An owner with identity key '" + identityKey + "' already exists");
    }

}
