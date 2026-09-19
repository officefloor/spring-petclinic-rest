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

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code identityKey}, the single value that drives duplicate detection.
 *
 * <p>The key is the SHA-256 hex digest of the owner's already-normalized telephone, already
 * lower-cased email and the {@link Soundex Soundex code} of the last name, joined as
 * {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName) + '|' + versionTag}.
 * The {@link IdentityVersion#TAG version tag} is mixed in so the version-2 key never coincides
 * with the value derived from the same owner under version 1. Two owners are hard duplicates only
 * when their <em>whole</em> keys are equal: because the telephone is part of the key, two members
 * of the same household with different telephones derive different keys and are both allowed (they
 * are instead flagged as a soft match).
 */
public final class OwnerIdentityKey {

    private static final String SEPARATOR = "|";

    private OwnerIdentityKey() {
    }

    /**
     * Compute the identity key for the given owner from its stored (already normalized) telephone
     * and email and the Soundex code of its last name. A {@code null} telephone or email
     * contributes an empty component.
     *
     * @param owner the owner to derive the key for
     * @return the owner's identity key as a 64-character lower-case hex string
     */
    public static String of(Owner owner) {
        String raw = orEmpty(owner.getTelephone())
            + SEPARATOR + orEmpty(owner.getEmail())
            + SEPARATOR + Soundex.encode(owner.getLastName())
            + SEPARATOR + IdentityVersion.TAG;
        return Sha256.hex(raw);
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
