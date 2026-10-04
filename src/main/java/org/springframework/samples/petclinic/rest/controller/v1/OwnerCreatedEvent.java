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

package org.springframework.samples.petclinic.rest.controller.v1;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Immutable structured audit event emitted on a successful owner create, alongside the human-readable
 * audit line. Serialized to JSON as {@code {seq, ownerId, memberId, membershipLevel, event}}.
 *
 * <p>{@code seq} is a monotonically increasing sequence number across creates. The
 * {@code primaryIdentifier} component carries the owner's <em>current primary identifier</em> (see
 * {@link org.springframework.samples.petclinic.model.Owner#getPrimaryIdentifier()}), the unified
 * {@code memberId}. The component is named for what it holds, while its JSON key is {@code memberId}.
 */
public record OwnerCreatedEvent(long seq, Integer ownerId,
                                @JsonProperty("memberId") String primaryIdentifier,
                                int membershipLevel, String event) {

    /** The fixed {@code event} discriminator carried by every owner-created event. */
    public static final String EVENT_TYPE = "OWNER_CREATED";
}
