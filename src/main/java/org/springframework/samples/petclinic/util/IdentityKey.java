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

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives the single key that identifies an owner for duplicate detection, namely
 * {@code normalizedTelephone + "|" + (email or empty) + "|" + (householdId or empty)}.
 * <p>
 * Two owners are duplicates exactly when their whole identity keys are equal. Because the
 * telephone is part of the key, owners in the same household (same {@code householdId}) with
 * different telephones have different keys and are not duplicates.
 * <p>
 * The values are taken as already normalized: the telephone in its stored E.164 form and the
 * email lower-cased, so the key computed at creation matches the one derived from stored fields.
 */
public final class IdentityKey {

    private IdentityKey() {
    }

    /** @return the identity key for {@code owner}. */
    public static String of(Owner owner) {
        return orEmpty(owner.getTelephone()) + "|" + orEmpty(owner.getEmail()) + "|"
            + orEmpty(owner.getHouseholdId());
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
