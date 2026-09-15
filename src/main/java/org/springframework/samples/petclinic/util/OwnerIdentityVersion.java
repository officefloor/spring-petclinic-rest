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

/**
 * The released version of the owner identity. Version 2 rederives every derived identifier
 * (the region code embedded in the member id, the member id, the household id and the identity
 * key) by {@link #tag(String) mixing in} the fixed {@link #TAG "V2"} version tag, so that no
 * value it produces coincides with the version-1 value for the same owner.
 * <p>
 * The version moves as a whole: the same release surfaces as the response's {@link #API_VERSION
 * apiVersion} and as the {@link #AUDIT_SCHEMA_VERSION schemaVersion} stamped on the structured
 * owner-created audit event. The tag is confined to the identifiers; user-facing region values
 * (locality, timezone and the owner segment's derived region) stay the plain region code.
 */
public final class OwnerIdentityVersion {

    /** Fixed version tag mixed into every derived identifier under identity version 2. */
    public static final String TAG = "V2";

    /** Identity version surfaced as the owner response's {@code apiVersion}. */
    public static final int API_VERSION = 2;

    /** Schema version stamped on the structured owner-created audit event. */
    public static final int AUDIT_SCHEMA_VERSION = 2;

    private OwnerIdentityVersion() {
    }

    /**
     * Mix the version {@link #TAG tag} into an identifier's derivation input by prefixing it, so
     * the value derived from {@code base} differs from the version-1 value derived from {@code base}
     * alone.
     *
     * @param base the plain derivation input (a region code, or the string fed to a digest)
     * @return {@code base} prefixed with the version tag
     */
    public static String tag(String base) {
        return TAG + base;
    }
}
