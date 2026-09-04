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

import java.util.List;

/**
 * Thrown when a request to create an owner carries an {@code identityKey} - the derived
 * {@code normalizedTelephone + '|' + email + '|' + householdId} - already used by another owner.
 * This single rule subsumes the previously separate telephone, email and household duplicate
 * checks: a create is a duplicate only when its whole identity key equals an existing owner's.
 * Carries the offending key so the {@link ExceptionControllerAdvice} can report the collision back
 * to the client in a {@code 409 Conflict} response.
 */
public class DuplicateOwnerIdentityException extends DuplicateOwnerException {

    private static final String DETAIL = "An owner with the given identity already exists";

    private final String identityKey;

    public DuplicateOwnerIdentityException(String identityKey) {
        super("Identity already in use by another owner: " + identityKey, DETAIL,
            List.of("telephone", "email", "lastName", "address"));
        this.identityKey = identityKey;
    }

    public String getIdentityKey() {
        return this.identityKey;
    }
}
