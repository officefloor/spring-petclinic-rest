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
 * Thrown when a request to create an owner carries an identity key that is already used by another
 * owner. The identity key is the single derived value consolidating the telephone, email and
 * household duplicate checks ({@code normalizedTelephone + '|' + (email or empty) + '|' +
 * householdId}); only an exact full-key match is a duplicate. Carries the offending identity key so
 * the {@link ExceptionControllerAdvice} can report it to the client, which answers with a 409 Conflict.
 */
public class DuplicateIdentityException extends RuntimeException {

    private final transient String identityKey;

    public DuplicateIdentityException(String identityKey) {
        super("Identity key already in use: " + identityKey);
        this.identityKey = identityKey;
    }

    public String getIdentityKey() {
        return this.identityKey;
    }
}
