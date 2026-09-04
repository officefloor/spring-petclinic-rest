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

import org.springframework.samples.petclinic.model.Owner;

/**
 * Signals that a new owner's normalized telephone is already used by an existing owner.
 * Mapped to HTTP 409 Conflict by {@link ExceptionControllerAdvice}.
 */
public class DuplicateTelephoneException extends RuntimeException {

    public DuplicateTelephoneException(String telephone) {
        super("Telephone already in use: " + telephone);
    }

    /** Reduce a telephone to its digits only, so equality ignores formatting differences. */
    private static String normalize(String telephone) {
        return telephone == null ? "" : telephone.replaceAll("\\D", "");
    }

    /**
     * Throw if {@code telephone} normalizes to the same digits as the telephone of any
     * owner in {@code existingOwners}.
     */
    public static void rejectIfDuplicate(String telephone, Collection<Owner> existingOwners) {
        String candidate = normalize(telephone);
        for (Owner owner : existingOwners) {
            if (candidate.equals(normalize(owner.getTelephone()))) {
                throw new DuplicateTelephoneException(telephone);
            }
        }
    }
}
