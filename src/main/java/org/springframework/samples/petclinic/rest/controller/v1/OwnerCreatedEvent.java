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

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Immutable structured audit event emitted (alongside the human-readable audit line) on each
 * successful owner create. It serializes to the JSON object
 * {@code {seq, ownerId, memberId, membershipLevel, event:'OWNER_CREATED'}}.
 *
 * <p>{@code seq} is a monotonically increasing integer across creates, letting a consumer order and
 * de-duplicate events. {@code memberId} carries the owner's primary identifier, the unified member id
 * (the controller derives the value through a single accessor, {@code primaryIdentifier}).
 *
 * <p>The event is a record, so it is immutable once constructed: its fields cannot be mutated after
 * the create that produced it, which is the guarantee an audit trail depends on.
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String memberId, Integer membershipLevel) {

    /** Fixed discriminator identifying this kind of audit event. */
    static final String EVENT_TYPE = "OWNER_CREATED";

    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    /**
     * Renders this event as its canonical JSON representation, with fields in the documented order
     * {@code {seq, ownerId, memberId, membershipLevel, event}}. This is the exact string published to
     * the {@code AUDIT} logger.
     *
     * @return the event as a single-line JSON object
     */
    public String toJson() {
        ObjectNode node = MAPPER.createObjectNode();
        node.put("seq", this.seq);
        node.put("ownerId", this.ownerId);
        node.put("memberId", this.memberId);
        node.put("membershipLevel", this.membershipLevel);
        node.put("event", EVENT_TYPE);
        return MAPPER.writeValueAsString(node);
    }
}
