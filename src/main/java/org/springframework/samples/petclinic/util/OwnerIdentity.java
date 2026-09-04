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

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derivation of an owner's {@code identityKey}, the single value all owner duplicate detection is
 * now expressed through. Kept separate from the owner controller and the {@code Owner} model so the
 * rule for deciding when two owners denote the same person lives in one place, as pure functions
 * with no web or persistence dependencies. Mirrors {@link TelephoneNormalizer},
 * {@link EmailNormalizer} and {@link Soundex}, whose comparison keys it composes.
 *
 * <p>The key is the SHA-256 hex digest of the owner's normalized telephone, lower-cased email (or
 * the empty string when absent) and the {@linkplain Soundex#soundex Soundex} of their last name,
 * joined with {@code '|'} separators before hashing:
 * {@code sha256hex(normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName))}. Two owners
 * are the same, and a create is rejected as a {@code 409 Conflict}, only when their whole keys are
 * equal — so the previously separate telephone, email and household duplicate checks are all
 * subsumed by this one key. Because the telephone is part of the key, two owners who share a last
 * name and postcode but carry different telephones have different keys: they are not a hard
 * duplicate but a {@linkplain OwnerIdentity soft match}. Only an exact full-key match is a
 * duplicate.
 */
public abstract class OwnerIdentity {

    /** Separates the telephone, email and last-name components hashed into the key. */
    private static final String SEPARATOR = "|";

    /**
     * Fixed version tag mixed into the hashed identity key under the version-2 identity algorithm, so
     * every key differs from the value the version-1 algorithm produced. Two owners are still the same
     * exactly when their whole keys are equal; the tag only shifts the derived key, not the rule for
     * when two owners collide.
     */
    private static final String VERSION_TAG = "V2";

    /**
     * Builds the identity key from an owner's raw parts. The telephone and email are reduced to
     * their {@linkplain TelephoneNormalizer#toComparisonKey comparison} /
     * {@linkplain EmailNormalizer#toComparisonKey comparison} forms and the last name to its
     * {@linkplain Soundex#soundex Soundex} code so values stored in different shapes still compose
     * equal keys; an absent (null or blank) email contributes the empty string, and an absent last
     * name codes to the empty string. The joined parts are then reduced to a
     * {@linkplain Sha256#hex SHA-256} hex digest.
     *
     * @param telephone the owner's telephone (normalized on create), or null
     * @param email     the owner's email (lower-cased on create), or null/blank when absent
     * @param lastName  the owner's last name, or null
     * @return the owner's identity key, a 64-character lower-case hex SHA-256 digest
     */
    public static String identityKey(String telephone, String email, String lastName) {
        return Sha256.hex(hashInput(telephone, email, lastName));
    }

    /**
     * The exact value the identity key digest is taken over: the telephone and email reduced to their
     * {@linkplain TelephoneNormalizer#toComparisonKey comparison} /
     * {@linkplain EmailNormalizer#toComparisonKey comparison} forms and the last name to its
     * {@linkplain Soundex#soundex Soundex} code, joined with {@code '|'} separators. Composing the
     * hashed value through this single method keeps the one place that decides what the identity key
     * covers explicit, mirroring how {@link HouseholdNormalizer#toComparisonKey} composes the value its
     * household id is hashed from.
     *
     * @param telephone the owner's telephone (normalized on create), or null
     * @param email     the owner's email (lower-cased on create), or null/blank when absent
     * @param lastName  the owner's last name, or null
     * @return the value hashed into the identity key
     */
    private static String hashInput(String telephone, String email, String lastName) {
        String telephonePart = telephone == null ? "" : TelephoneNormalizer.toComparisonKey(telephone);
        String emailPart = (email == null || email.isBlank()) ? "" : EmailNormalizer.toComparisonKey(email);
        String lastNamePart = Soundex.soundex(lastName);
        return VERSION_TAG + SEPARATOR + telephonePart + SEPARATOR + emailPart + SEPARATOR + lastNamePart;
    }

    /**
     * Builds the identity key from a persisted owner's stored telephone, email and last name.
     *
     * @param owner the owner (non-null)
     * @return the owner's identity key
     */
    public static String identityKey(Owner owner) {
        return identityKey(owner.getTelephone(), owner.getEmail(), owner.getLastName());
    }

}
