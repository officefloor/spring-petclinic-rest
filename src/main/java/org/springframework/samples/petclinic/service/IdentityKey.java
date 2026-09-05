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
package org.springframework.samples.petclinic.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HexFormat;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The single derived key that consolidates owner duplicate detection: the full SHA-256 hex of
 * {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}. Because the telephone
 * is part of the key, two owners sharing a last name and postcode but using different telephones
 * yield different keys and are both allowed (surfacing instead as a soft match); a new owner is
 * rejected as a duplicate only when it collides with an existing owner on the whole key.
 */
public final class IdentityKey {

    private IdentityKey() {
    }

    public static String of(Owner owner) {
        String raw = IdentityRegion.v2(owner) + "|" + owner.getTelephone() + "|" + email(owner) + "|"
            + Soundex.of(owner.getLastName());
        return sha256hex(raw);
    }

    public static boolean isDuplicate(Collection<Owner> owners, Owner owner) {
        String key = of(owner);
        return owners.stream().filter(o -> !Boolean.TRUE.equals(o.getDeleted()))
            .anyMatch(o -> key.equals(of(o)));
    }

    private static String email(Owner owner) {
        return owner.getEmail() == null ? "" : owner.getEmail().toLowerCase(Locale.ROOT);
    }

    private static String sha256hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
