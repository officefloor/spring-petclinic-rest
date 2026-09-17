/*
 * Copyright 2002-2017 the original author or authors.
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
 * Owns version 2 of the owner identity: the version number surfaced as the response
 * {@code apiVersion} and the audit {@code schemaVersion}, and the fixed {@code "V2"} tag that every
 * version-2 identifier mixes in.
 *
 * <p>Mixing the tag into each identifier's derivation guarantees that no value produced under
 * version 1 is ever reproduced under version 2. The tag is mixed only into the derived identifiers
 * — the memberId's region segment and the hash inputs of the householdId and identityKey; the
 * user-facing locality, timezone and owner segment keep the plain region and never carry the tag.
 */
public final class OwnerIdentityVersion {

    /**
     * The current owner-identity version, surfaced as the owner response {@code apiVersion} and the
     * audit event {@code schemaVersion}.
     */
    public static final int VERSION = 2;

    /** The fixed version tag mixed into every version-2 identifier. */
    public static final String TAG = "V2";

    private OwnerIdentityVersion() {
    }

    /**
     * Mix the version tag into a hash input, so a version-2 digest can never collide with the
     * version-1 digest of the same owner.
     *
     * @param input the version-1 hash input
     * @return the input prefixed with the version tag and a separator
     */
    public static String stampHashInput(String input) {
        return TAG + "|" + input;
    }

    /**
     * Derive the region code embedded inside identifiers from the plain region, appending the version
     * tag so the identifier's region segment differs from the plain region kept by the locality.
     *
     * @param plainRegion the plain, user-facing region (for example {@code "NSW"})
     * @return the version-2 region code embedded in identifiers (for example {@code "NSWV2"})
     */
    public static String regionCode(String plainRegion) {
        return plainRegion + TAG;
    }
}
