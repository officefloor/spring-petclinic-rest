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
package org.springframework.samples.petclinic.model;

/**
 * The version of the owner-identity derivation. Version 2 mixes the fixed {@link #TAG}
 * into every derived identifier (the member id, household id and identity key) so that
 * each differs from its version-1 value and no version-1 value is ever produced again.
 *
 * <p>The tag lives only inside the identifiers: the user-facing locality, timezone and
 * owner segment keep the plain region (see {@link RegionResolver}). The same {@link #NUMBER}
 * is reported as the owner response's {@code apiVersion} and the audit event's
 * {@code schemaVersion}, so the version is stated in exactly one place.
 */
public final class IdentityVersion {

    /** The numeric identity version, reported as the response apiVersion and audit schemaVersion. */
    public static final int NUMBER = 2;

    /** The fixed version tag mixed into every derived identifier. */
    public static final String TAG = "V2";

    private IdentityVersion() {
    }
}
