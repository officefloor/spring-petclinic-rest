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
 * Thrown when an owner is created with a lower-cased email that is already used by another
 * owner.
 * <p>
 * The exception handler reports this as a 409 Conflict, since the request collides with the
 * current state of an existing resource.
 */
public class DuplicateOwnerEmailException extends RuntimeException {

    private final String email;

    public DuplicateOwnerEmailException(String email) {
        super("An owner with email '" + email + "' already exists");
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }
}
