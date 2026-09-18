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

package org.springframework.samples.petclinic.rest.validation;

/**
 * Raised when a new owner's identity key (normalized telephone, email and household id, see
 * {@code Owner#getIdentityKey()}) exactly matches that of an existing owner, so the client can be
 * told the record duplicates another one.
 */
public class DuplicateIdentityException extends RuntimeException {

    private final String identityKey;

    public DuplicateIdentityException(String identityKey) {
        super("An owner with the same identity key already exists: " + identityKey);
        this.identityKey = identityKey;
    }

    public String getIdentityKey() {
        return this.identityKey;
    }
}
