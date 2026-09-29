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
package org.springframework.samples.petclinic.service;

/**
 * Thrown when an {@link org.springframework.samples.petclinic.model.Owner} is
 * created (or updated) with a normalized telephone number that is already used
 * by another owner. Signals a business-rule conflict, mapped to HTTP 409.
 */
public class DuplicateTelephoneException extends RuntimeException {

    public DuplicateTelephoneException(String telephone) {
        super("An owner with telephone '" + telephone + "' already exists");
    }
}
