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

package org.springframework.samples.petclinic.util;

import java.util.Collection;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.advice.DuplicateTelephoneException;

/**
 * Derives an owner's {@code identityKey}: {@code normalizedTelephone|email|householdId}.
 * All duplicate detection is consolidated here: two owners are the same registration only
 * when their WHOLE identity keys are equal, and a new owner is rejected only on a full match.
 */
public final class IdentityKey {

    private IdentityKey() {
    }

    /**
     * {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}. An owner carries no
     * shared-household membership in its stored state, so the household component is empty and two
     * owners differ in identity only through their telephone and email.
     */
    public static String of(Owner owner) {
        return telephone(owner.getTelephone()) + "|" + email(owner.getEmail()) + "|";
    }

    /** Reject with 409 when {@code candidate}'s whole identity key equals an existing owner's. */
    public static void rejectIfDuplicate(Owner candidate, Collection<Owner> existingOwners) {
        String key = of(candidate);
        for (Owner owner : existingOwners) {
            if (owner.isDeleted()) {
                continue;
            }
            if (key.equals(of(owner))) {
                throw new DuplicateTelephoneException(key);
            }
        }
    }

    /** Digits only, so equality ignores telephone formatting differences. */
    private static String telephone(String telephone) {
        return telephone == null ? "" : telephone.replaceAll("\\D", "");
    }

    /** Lower-cased and trimmed, so equality ignores case; blank/absent emails compare equal. */
    private static String email(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
