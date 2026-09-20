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
 * The owner-identity algorithm version. Version 2 mixes the fixed {@link #TAG} into every
 * derived identifier — the {@code memberId} region code, the {@code identityKey} and the
 * {@code householdId} — so all identifiers change between versions and no value produced under
 * version 1 is ever produced again.
 * <p>
 * The tag is an identifier-only concern: the user-facing {@code locality}, {@code timezone} and
 * owner segment keep the plain region code and never carry the tag. The version is surfaced to
 * clients as the response's {@code apiVersion}.
 */
public final class OwnerIdentityVersion {

    /** Current owner-identity / API version, surfaced to clients as {@code apiVersion}. */
    public static final int CURRENT = 2;

    /** Fixed version tag mixed into every version-2 identifier. */
    public static final String TAG = "V2";

    private OwnerIdentityVersion() {
    }
}
