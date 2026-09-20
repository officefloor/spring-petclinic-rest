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

package org.springframework.samples.petclinic.rest.controller;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.OwnerIdentityVersion;
import org.springframework.stereotype.Component;

/**
 * Owner identity: the single derived key that consolidates telephone, email and last name into
 * one value used for duplicate detection. Two owners are the same identity only when their whole
 * key matches; because the (normalized) telephone is part of the key, two members of one household
 * with different telephones have distinct identities.
 * <p>
 * The key is the SHA-256 hex digest of {@code V2 + '|' + normalizedTelephone + '|' + lowerEmail +
 * '|' + soundex(lastName)}, built from the owner's already-canonical fields (telephone in E.164
 * form, email trimmed and lower-cased) with the {@link OwnerIdentityVersion#TAG version tag} mixed
 * in as the leading segment. A {@code null} field contributes an empty segment so distinct fields
 * cannot collide across the {@code '|'} separators.
 */
@Component
public class OwnerIdentity {

    private final Soundex soundex;

    private final Sha256 sha256;

    public OwnerIdentity(Soundex soundex, Sha256 sha256) {
        this.soundex = soundex;
        this.sha256 = sha256;
    }

    /**
     * The identity key for {@code owner}: the SHA-256 hex of the {@link OwnerIdentityVersion#TAG
     * version tag}, its normalized telephone, lower-cased email and the Soundex code of its last
     * name.
     */
    public String key(Owner owner) {
        String raw = OwnerIdentityVersion.TAG
            + "|" + segment(owner.getTelephone())
            + "|" + segment(owner.getEmail())
            + "|" + soundex.of(owner.getLastName());
        return sha256.hex(raw);
    }

    private String segment(String value) {
        return value == null ? "" : value;
    }
}
