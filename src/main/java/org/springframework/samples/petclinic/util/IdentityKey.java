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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.advice.DuplicateTelephoneException;

/**
 * Derives an owner's {@code identityKey}: the SHA-256 hex of
 * {@code normalizedTelephone|lowerEmail|soundex(lastName)}. All duplicate detection is
 * consolidated here: two owners are the same registration only when their identity keys are
 * equal, and a new owner is rejected only on a full match.
 */
public final class IdentityKey {

    private IdentityKey() {
    }

    /**
     * The lower-case SHA-256 hex over
     * {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}.
     */
    public static String of(Owner owner) {
        String raw = CustomerCode.VERSION_TAG + "|" + telephone(owner.getTelephone()) + "|" + email(owner.getEmail())
                + "|" + Soundex.of(owner.getLastName());
        return sha256Hex(raw);
    }

    private static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(64);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
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
