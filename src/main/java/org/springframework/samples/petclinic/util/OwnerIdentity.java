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
 * {@link EmailNormalizer} and {@link HouseholdNormalizer}, whose comparison keys it composes.
 *
 * <p>The key is the owner's normalized telephone, email (or the empty string when absent) and
 * household id joined with {@code '|'} separators:
 * {@code normalizedTelephone + '|' + email + '|' + householdId}. Two owners are the same, and a
 * create is rejected as a {@code 409 Conflict}, only when their whole keys are equal — so the
 * previously separate telephone, email and household duplicate checks are all subsumed by this one
 * key. Because the telephone is part of the key, two members of the same household (same
 * {@code householdId}) with different telephones have different keys and are both allowed; only an
 * exact full-key match is a duplicate.
 */
public abstract class OwnerIdentity {

    /** Separates the telephone, email and household components of the key. */
    private static final String SEPARATOR = "|";

    /**
     * Builds the identity key from an owner's raw parts. The telephone and email are reduced to
     * their {@linkplain TelephoneNormalizer#toComparisonKey comparison} /
     * {@linkplain EmailNormalizer#toComparisonKey comparison} forms so values stored in different
     * shapes still compose equal keys; an absent (null or blank) email contributes the empty string,
     * and an absent household id likewise contributes the empty string.
     *
     * @param telephone   the owner's telephone (normalized on create), or null
     * @param email       the owner's email (lower-cased on create), or null/blank when absent
     * @param householdId the owner's household id, or null
     * @return the owner's identity key
     */
    public static String identityKey(String telephone, String email, String householdId) {
        String telephonePart = telephone == null ? "" : TelephoneNormalizer.toComparisonKey(telephone);
        String emailPart = (email == null || email.isBlank()) ? "" : EmailNormalizer.toComparisonKey(email);
        String householdPart = householdId == null ? "" : householdId;
        return telephonePart + SEPARATOR + emailPart + SEPARATOR + householdPart;
    }

    /**
     * Builds the identity key from a persisted owner's stored telephone, email and household id.
     *
     * @param owner the owner (non-null)
     * @return the owner's identity key
     */
    public static String identityKey(Owner owner) {
        return identityKey(owner.getTelephone(), owner.getEmail(), owner.getHouseholdId());
    }

}
