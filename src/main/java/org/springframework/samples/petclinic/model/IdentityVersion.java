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
package org.springframework.samples.petclinic.model;

/**
 * The version of the owner identity — the derivation of the values that identify an owner
 * (its {@link MemberId member id}, identity key and household id).
 * <p>
 * Version 2 mixes a fixed {@code "V2"} version {@link #TAG tag} into the region code carried
 * inside those identifiers, so every version-2 identifier differs from the value version 1
 * produced for the same owner and no version-1 value is ever produced again. The tag lives
 * only inside the identifiers: the region code is recovered in plain form (e.g. {@code "NSW"})
 * with {@link #plainRegion(String)} for the user-facing locality, timezone and owner segment,
 * which never carry the tag.
 */
public final class IdentityVersion {

    /** The current owner-identity version, exposed as the API and audit-schema version. */
    public static final int NUMBER = 2;

    /** The fixed version tag mixed into the region code inside identifiers, e.g. {@code "V2"}. */
    public static final String TAG = "V" + NUMBER;

    private IdentityVersion() {
    }

    /**
     * The version-2 region code for {@code region}: the plain region with the version
     * {@link #TAG tag} mixed in, e.g. {@code "V2NSW"} for {@code "NSW"}. This is the region
     * code carried inside the owner's identifiers.
     *
     * @param region the plain region (e.g. {@code "NSW"})
     * @return the version-2 region code
     */
    public static String regionCode(String region) {
        return TAG + region;
    }

    /**
     * The plain region encoded in a version-2 {@code regionCode}: the region with the version
     * {@link #TAG tag} stripped, e.g. {@code "NSW"} for {@code "V2NSW"}. A code without the tag
     * is returned unchanged.
     *
     * @param regionCode a version-2 region code (e.g. {@code "V2NSW"})
     * @return the plain region it encodes
     */
    public static String plainRegion(String regionCode) {
        return regionCode != null && regionCode.startsWith(TAG)
            ? regionCode.substring(TAG.length()) : regionCode;
    }
}
