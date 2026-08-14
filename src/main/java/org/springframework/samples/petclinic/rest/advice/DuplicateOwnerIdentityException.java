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
 * Raised when an owner is created whose derived identity key (the SHA-256 hex digest of
 * {@code '<normalizedTelephone>|<lowerEmail>|<soundex(lastName)>'}) exactly equals that of an existing
 * owner. This single key consolidates the former separate telephone, email and household duplicate
 * checks. Maps to a 409 Conflict response.
 */
public class DuplicateOwnerIdentityException extends RuntimeException {

    private final String identityKey;

    public DuplicateOwnerIdentityException(String identityKey) {
        super("An owner with identity key " + identityKey + " already exists");
        this.identityKey = identityKey;
    }

    public String getIdentityKey() {
        return this.identityKey;
    }
}
