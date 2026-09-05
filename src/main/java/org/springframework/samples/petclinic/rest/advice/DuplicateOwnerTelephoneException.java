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
 * Raised when an owner submitted to the create endpoint has a normalized telephone that is already
 * used by another existing owner. The {@code ExceptionControllerAdvice} renders this as a 409
 * Conflict response.
 */
public class DuplicateOwnerTelephoneException extends RuntimeException {

    private final String telephone;

    public DuplicateOwnerTelephoneException(String telephone) {
        super("Telephone already in use: " + telephone);
        this.telephone = telephone;
    }

    /**
     * @return the normalized telephone that collided with an existing owner
     */
    public String getTelephone() {
        return this.telephone;
    }
}
