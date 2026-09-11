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

package org.springframework.samples.petclinic.util;

/**
 * Derives an owner's identity key: the value duplicate detection compares owners by. It is the
 * 64-character lower-case hex SHA-256 digest over the fixed {@code "V2"} identity version tag and
 * the owner's stable identity fields — the normalized telephone, the lower-cased email (or an empty
 * string when absent) and the Soundex code of the last name — joined with {@code '|'}, hashed via
 * the shared {@link Sha256Hex} helper so every hash-derived identifier in the app is computed one
 * way. The version tag is mixed into every key, so it changes every value from its version-1 form
 * without affecting duplicate detection: two owners are duplicates only when their whole identity
 * keys are equal.
 * <p>
 * Keeping the identity key's derivation in a focused util gathers it alongside the other derived
 * owner identifiers ({@link MemberId}, {@link HouseholdNormalizer}) rather than inline in the
 * {@code Owner} entity, so the identity fields that feed the key, and how they are combined, have a
 * single home.
 */
public final class IdentityKey {

    /**
     * The fixed identity version tag mixed into the hashed identity fields so the version-2 identity
     * key differs from every value produced under version 1. It is prepended to the joined fields
     * before hashing; because the same tag is mixed into every key, duplicate detection is unchanged
     * (two owners still collide only when their whole keys are equal).
     */
    private static final String IDENTITY_VERSION_TAG = "V2";

    private IdentityKey() {
    }

    /**
     * Derives the identity key from an owner's stable identity fields. The telephone and email are
     * expected already normalized (the email lower-cased); a {@code null} telephone or email is
     * treated as the empty string so an owner without an email still yields a well-formed key.
     *
     * @param telephone the owner's normalized telephone, or {@code null}
     * @param email     the owner's lower-cased email, or {@code null} when absent
     * @param lastName  the owner's last name, used via its Soundex code
     * @return the 64-character lower-case hex identity key
     */
    public static String of(String telephone, String email, String lastName) {
        String normalizedTelephone = telephone == null ? "" : telephone;
        String lowerEmail = email == null ? "" : email;
        return Sha256Hex.lowerHex(IDENTITY_VERSION_TAG + '|' + normalizedTelephone + '|' + lowerEmail
            + '|' + Soundex.encode(lastName));
    }
}
