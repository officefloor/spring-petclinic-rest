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
 * Thrown when a request to create an owner supplies an email whose lower-cased form is already
 * used by another owner. Carries the offending email so the REST response can report the
 * conflicting value.
 */
public class DuplicateOwnerEmailException extends RuntimeException {

    private final String email;

    public DuplicateOwnerEmailException(String email) {
        super("Email already in use by another owner: " + email);
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }
}
