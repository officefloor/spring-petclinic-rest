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

package org.springframework.samples.petclinic.rest.error;

/**
 * Thrown when an owner is created whose whole identity key (normalized telephone, email and
 * household id) matches that of an existing owner. Carries the conflicting key so the REST layer
 * can report what was rejected.
 */
public class DuplicateOwnerException extends RuntimeException {

    private final String identityKey;

    public DuplicateOwnerException(String identityKey) {
        super("An owner with identity key '" + identityKey + "' already exists");
        this.identityKey = identityKey;
    }

    public String getIdentityKey() {
        return this.identityKey;
    }
}
