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

import java.util.Collection;
import java.util.Objects;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The single derived key that consolidates owner duplicate detection. It is exposed as
 * {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}. Because the
 * telephone is part of the key, two members of the same household with different telephones
 * yield different keys and are both allowed; a new owner is rejected as a duplicate only when
 * it collides with an existing owner on the same telephone and email.
 */
public final class IdentityKey {

    private IdentityKey() {
    }

    public static String of(Owner owner) {
        return owner.getTelephone() + "|" + email(owner) + "|" + HouseholdMatcher.householdId(owner);
    }

    public static boolean isDuplicate(Collection<Owner> owners, Owner owner) {
        return owners.stream().anyMatch(o -> Objects.equals(owner.getTelephone(), o.getTelephone())
            && email(owner).equals(email(o)));
    }

    private static String email(Owner owner) {
        return owner.getEmail() == null ? "" : owner.getEmail();
    }
}
