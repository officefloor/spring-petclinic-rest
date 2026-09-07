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
 * Thrown when a request tries to create an owner whose derived identity key (the
 * normalized telephone, email and household identifier joined by '|') is already
 * used by another owner.
 * <p>
 * Signals a conflict with existing data so the {@link ExceptionControllerAdvice}
 * can surface it to the client as a {@code 409 Conflict} response.
 */
public class DuplicateIdentityException extends RuntimeException {

    public DuplicateIdentityException(String identityKey) {
        super("An owner with identity key '" + identityKey + "' already exists");
    }
}
