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

package org.springframework.samples.petclinic.rest.validation;

/**
 * Thrown when an owner is created whose derived {@code identityKey} (the SHA-256 hex over
 * {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}) exactly matches
 * that of an existing owner. This single key consolidates the former separate telephone,
 * e-mail and household duplicate checks. Handled by the REST {@code ExceptionControllerAdvice},
 * which translates it into a {@code 409 Conflict} response.
 */
public class DuplicateIdentityException extends RuntimeException {

    public DuplicateIdentityException(String message) {
        super(message);
    }
}
