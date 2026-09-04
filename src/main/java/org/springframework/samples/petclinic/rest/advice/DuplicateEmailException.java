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

import java.util.Collection;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Signals that a new owner's lower-cased email is already used by an existing owner.
 * Mapped to HTTP 409 Conflict by {@link ExceptionControllerAdvice}.
 */
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super("Email already in use: " + email);
    }

    /** Lower-case an email so equality ignores case; blank/absent emails never match. */
    private static String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Throw if {@code email} lower-cases to the same value as the email of any owner in
     * {@code existingOwners}. A blank candidate email is treated as absent and never rejected.
     */
    public static void rejectIfDuplicate(String email, Collection<Owner> existingOwners) {
        String candidate = normalize(email);
        if (candidate.isEmpty()) {
            return;
        }
        for (Owner owner : existingOwners) {
            if (candidate.equals(normalize(owner.getEmail()))) {
                throw new DuplicateEmailException(email);
            }
        }
    }
}
