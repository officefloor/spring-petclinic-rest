/*
 * Copyright 2002-2013 the original author or authors.
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

import org.springframework.samples.petclinic.model.Owner;

/** Soft-match detection for a new owner against the existing owners. */
public final class PossibleDuplicates {

    private PossibleDuplicates() {
    }

    /**
     * Id of an existing (non-deleted) owner this one is a suspected (soft) duplicate of, or {@code null}.
     *
     * <p>A soft match is an owner whose {@link IdentityKeys#of identityKey} differs (so it is not a hard
     * 409 duplicate) yet whose {@code soundex(lastName)} and postcode both match: the same household
     * reached under a different telephone or email.
     */
    public static Integer matchIn(Owner owner, Collection<Owner> existing) {
        String key = IdentityKeys.of(owner);
        String soundex = IdentityKeys.soundex(owner.getLastName());
        String postcode = owner.getPostcode();
        for (Owner other : existing) {
            if (!other.isDeleted() && postcode != null && postcode.equals(other.getPostcode())
                && soundex.equals(IdentityKeys.soundex(other.getLastName()))
                && !key.equals(IdentityKeys.of(other))) {
                return other.getId();
            }
        }
        return null;
    }
}
