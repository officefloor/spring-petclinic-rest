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
 * Signals that a new owner could not be created because its lower-cased email address is
 * already used by an existing owner.
 *
 * <p>Handled as a 409 Conflict, reporting {@code email} as the offending field.
 */
public class DuplicateOwnerEmailException extends RuntimeException {

    public DuplicateOwnerEmailException(String email) {
        super("Email already in use by another owner: " + email);
    }
}
