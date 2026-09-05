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

import org.springframework.samples.petclinic.model.Owner;

/**
 * The region code fed into the version-2 owner identifiers. It takes the plain region and
 * mixes in the fixed 'V2' version tag, so every identifier that hashes it in (memberId,
 * householdId, identityKey) differs from its version-1 value and no version-1 value is
 * reproduced. The tag lives only here, inside the identifiers; the user-facing locality,
 * timezone and owner segment keep reading the plain region through {@link RegionCode}.
 */
public final class IdentityRegion {

    private IdentityRegion() {
    }

    public static String v2(Owner owner) {
        return "V2|" + LocalityResolver.locality(owner.getCity(), owner.getPostcode());
    }
}
