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

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives the single key that identifies an owner for duplicate detection: the lower-case,
 * 64-character hex SHA-256 digest over
 * {@code normalizedTelephone + "|" + lowerEmail + "|" + soundex(lastName)}.
 * <p>
 * Two owners are duplicates exactly when their whole identity keys are equal. Because the
 * telephone is part of the key, owners with the same last name and postcode but different
 * telephones have different keys and are not hard duplicates; they are instead flagged as soft
 * (possible) duplicates. The household id no longer takes part in identity: household grouping is a
 * separate concern.
 * <p>
 * The telephone is taken in its stored E.164 form; the email is lower-cased and the last name is
 * reduced to its {@link Soundex} code here, so the key computed at creation matches the one derived
 * from stored fields.
 */
public final class IdentityKey {

    private IdentityKey() {
    }

    /** @return the identity key for {@code owner}. */
    public static String of(Owner owner) {
        String raw = orEmpty(owner.getTelephone()) + "|"
            + orEmpty(owner.getEmail()).toLowerCase(Locale.ROOT) + "|"
            + Soundex.encode(owner.getLastName());
        return Sha256Hex.lowerHex(raw);
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
