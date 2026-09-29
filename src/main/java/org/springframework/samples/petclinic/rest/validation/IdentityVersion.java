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

package org.springframework.samples.petclinic.rest.validation;

/**
 * The single source of truth for version 2 of the owner identity. The {@link #TAG version tag} is
 * mixed into every derived <em>identifier</em> — the {@code memberId} (including the region code
 * embedded inside it), the {@code householdId} and the {@code identityKey} — so that no value
 * produced under version 1 is ever produced again. {@link #API_VERSION} is the integer surfaced as
 * the owner response's {@code apiVersion} and the audit event's {@code schemaVersion}.
 *
 * <p>The tag is an identifier-only concern: it must never leak into the user-facing {@code locality}
 * or {@code timezone}, nor into the region the owner segment is derived from, which all remain the
 * plain region code (e.g. {@code NSW}).
 */
public final class IdentityVersion {

    /** The integer owner-identity contract version, surfaced as {@code apiVersion}/{@code schemaVersion}. */
    public static final int API_VERSION = 2;

    /** The fixed version tag mixed into every derived identifier. */
    public static final String TAG = "V2";

    private IdentityVersion() {
    }

    /**
     * The version-2 region code used <em>inside</em> the identifiers: the plain region code (as
     * returned by {@link Regions#forPostcode(String)}) prefixed with the {@link #TAG version tag}.
     * This never equals a plain region code, so it must not be read back as the user-facing locality.
     *
     * @param region the plain region code (e.g. {@code NSW} or {@link Regions#UNKNOWN})
     * @return the version-2 region code (e.g. {@code V2NSW})
     */
    public static String regionCode(String region) {
        return TAG + region;
    }
}
