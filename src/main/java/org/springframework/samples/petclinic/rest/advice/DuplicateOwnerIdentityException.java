/*
 * Copyright 2016-2017 the original author or authors.
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
 * Thrown when a request to create an owner produces an {@code identityKey}
 * ({@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}) that exactly
 * matches an existing owner's. This single derived key consolidates the former separate
 * telephone, email and household duplicate checks: only an exact whole-key match is a
 * duplicate. Carries the offending key so the REST response can report the conflicting value.
 */
public class DuplicateOwnerIdentityException extends RuntimeException {

    private final String identityKey;

    public DuplicateOwnerIdentityException(String identityKey) {
        super("Owner identity already in use by another owner: " + identityKey);
        this.identityKey = identityKey;
    }

    public String getIdentityKey() {
        return this.identityKey;
    }
}
