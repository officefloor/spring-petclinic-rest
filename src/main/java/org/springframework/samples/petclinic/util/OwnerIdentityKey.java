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
 * <p>The key joins the owner's already-normalized telephone, email and household identifier as
 * {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}. Two owners are
 * duplicates only when their <em>whole</em> keys are equal: because the telephone is part of the
 * key, two members of the same household with different telephones derive different keys and are
 * both allowed.
 */
public final class OwnerIdentityKey {

    private static final String SEPARATOR = "|";

    private OwnerIdentityKey() {
    }

    /**
     * Compute the identity key for the given owner from its stored (already normalized) telephone,
     * email and household identifier. A {@code null} email or household identifier contributes an
     * empty component.
     *
     * @param owner the owner to derive the key for
     * @return the owner's identity key
     */
    public static String of(Owner owner) {
        return orEmpty(owner.getTelephone())
            + SEPARATOR + orEmpty(owner.getEmail())
            + SEPARATOR + orEmpty(owner.getHouseholdId());
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
