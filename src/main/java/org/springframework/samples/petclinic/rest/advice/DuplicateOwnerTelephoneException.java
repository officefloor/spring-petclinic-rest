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
 * Thrown when a request to create an owner carries a normalized telephone that is already used by
 * another owner. Carries the offending (normalized) telephone so the {@link ExceptionControllerAdvice}
 * can report it back to the client in a {@code 409 Conflict} response.
 */
public class DuplicateOwnerTelephoneException extends DuplicateOwnerException {

    private static final String DETAIL = "An owner with the given telephone already exists";

    private final String telephone;

    public DuplicateOwnerTelephoneException(String telephone) {
        super("Telephone already in use by another owner: " + telephone, DETAIL, List.of("telephone"));
        this.telephone = telephone;
    }

    public String getTelephone() {
        return this.telephone;
    }
}
