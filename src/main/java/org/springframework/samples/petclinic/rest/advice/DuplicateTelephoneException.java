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
 * Thrown when a request to create an owner carries a normalized telephone number that is already
 * used by another owner. Carries the offending normalized value so the
 * {@link ExceptionControllerAdvice} can report it to the client, which answers with a 409 Conflict.
 */
public class DuplicateTelephoneException extends RuntimeException {

    private final transient String telephone;

    public DuplicateTelephoneException(String telephone) {
        super("Telephone already in use: " + telephone);
        this.telephone = telephone;
    }

    public String getTelephone() {
        return this.telephone;
    }
}
