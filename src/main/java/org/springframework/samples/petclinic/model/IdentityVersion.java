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
 * The single source of truth for the current owner-identity version. Bumping {@link #VERSION} moves
 * every identifier (the {@code memberId}, {@code householdId} and {@code identityKey}), the owner
 * response's {@code apiVersion} and the audit event's {@code schemaVersion} together, so the identity
 * scheme evolves as one unit.
 *
 * <p>The {@link #TAG} is mixed into the derivation of every identifier so that no value produced
 * under an earlier version is ever produced again. It appears only inside the identifiers: the
 * user-facing region ({@code locality}, {@code timezone} and the owner segment's derived region)
 * stays the plain region code.
 */
public final class IdentityVersion {

    /** The current identity version, shared by the identifiers, the API response and the audit event. */
    public static final int VERSION = 2;

    /** The version tag mixed into every identifier's derivation (e.g. {@code "V2"}). */
    public static final String TAG = "V" + VERSION;

    private IdentityVersion() {
    }
}
