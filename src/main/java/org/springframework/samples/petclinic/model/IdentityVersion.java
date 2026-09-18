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
 * Version 2 of the owner identity release.
 *
 * <p>Owns the fixed {@value #TAG} version tag that a version-2 identifier mixes in, so no identifier
 * value produced under version 1 is ever produced again, and the {@link #NUMBER version number}
 * surfaced to clients (the response {@code apiVersion} and the audit {@code schemaVersion}).
 *
 * <p>The tag belongs to the identifiers alone: it is mixed into the hashed identifiers (the household
 * id and identity key) and into the {@linkplain #regionCode(String) region code} embedded in the
 * member id, but it is {@linkplain #plainRegion(String) stripped back off} the region for the
 * user-facing values, so {@code locality}, {@code timezone} and the owner-segment region stay the
 * plain region code (e.g. {@code 'NSW'}).
 */
public final class IdentityVersion {

    /** The identity release version: the response {@code apiVersion} and the audit {@code schemaVersion}. */
    public static final int NUMBER = 2;

    /** The fixed version tag a version-2 identifier mixes in. */
    public static final String TAG = "V2";

    private IdentityVersion() {
    }

    /**
     * Mix the version tag into the key of a hashed identifier so its digest differs from the value
     * version 1 hashed from the same owner fields.
     *
     * @param key the version-1 key hashed to produce the identifier
     * @return the key with the version tag mixed in
     */
    public static String mix(String key) {
        return TAG + '|' + key;
    }

    /**
     * The version-2 region code embedded inside the member id: the plain region tagged with the
     * version tag, so it can never coincide with a version-1 region.
     *
     * @param plainRegion the plain region code (e.g. {@code 'NSW'})
     * @return the tagged region code (e.g. {@code 'V2NSW'})
     */
    public static String regionCode(String plainRegion) {
        return TAG + plainRegion;
    }

    /**
     * The plain region code carried by a version-2 {@linkplain #regionCode(String) region code}, i.e.
     * the tag stripped back off. A value carrying no tag is returned unchanged, and {@code null} maps
     * to {@code null}.
     *
     * @param regionCode the region code read back from a member id (may be {@code null})
     * @return the plain region code, or {@code null}
     */
    public static String plainRegion(String regionCode) {
        if (regionCode == null) {
            return null;
        }
        return regionCode.startsWith(TAG) ? regionCode.substring(TAG.length()) : regionCode;
    }
}
