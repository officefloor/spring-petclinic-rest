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

package org.springframework.samples.petclinic.util;

/**
 * The current version of the owner identity, shared by everything that derives or reports it.
 *
 * <p>{@link #TAG} is mixed into every derived identifier (the region code carried inside the
 * {@code memberId}, the {@code householdId} and the {@code identityKey}) so that version-2 values
 * never coincide with the version-1 values derived from the same owner. {@link #VERSION} is the
 * matching numeric version surfaced in the API response ({@code apiVersion}) and the audit event
 * ({@code schemaVersion}).
 */
public final class IdentityVersion {

    /** The current identity version number, surfaced as {@code apiVersion} and {@code schemaVersion}. */
    public static final int VERSION = 2;

    /** The fixed version tag mixed into every version-2 identifier so no version-1 value recurs. */
    public static final String TAG = "V2";

    private IdentityVersion() {
    }
}
