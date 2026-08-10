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
 * Thrown on create when a new owner's whole derived {@code identityKey} (the normalized telephone,
 * the email and the household identifier joined by {@code '|'}) equals that of an existing owner.
 * This single key consolidates the former separate telephone, email and household duplicate checks:
 * only an exact full-key match is a duplicate, so two owners that differ in any one segment (for
 * example two members of the same household with different telephones) are both allowed. Handled by
 * the {@link ExceptionControllerAdvice} as a 409 Conflict.
 */
public class DuplicateIdentityException extends RuntimeException {

    public DuplicateIdentityException(String message) {
        super(message);
    }
}
