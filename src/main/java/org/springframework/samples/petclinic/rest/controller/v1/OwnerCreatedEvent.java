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

/**
 * Immutable structured audit event recording that an owner was created. Emitted to the dedicated {@code AUDIT} logger as
 * a JSON object alongside the human-readable audit line (see
 * {@link OwnerRestControllerV1#auditOwnerCreated(org.springframework.samples.petclinic.model.Owner)}).
 *
 * <p>The event carries the owner's <em>current primary identifier</em> in its {@code primaryId} field: today that is the
 * owner's {@code customerCode}, and whatever replaces it later (for instance a unified {@code memberId}) flows through
 * the same field unchanged, because the field records "the owner's identity" rather than any one named column. The JSON
 * key stays {@code customerCode} for now, matching the identifier it carries.
 *
 * @param seq         monotonically increasing sequence number across all owner creates
 * @param ownerId     the newly created owner's generated id
 * @param primaryId   the owner's current primary identifier (the {@code customerCode} today)
 * @param membershipLevel the owner's membership level at creation
 */
record OwnerCreatedEvent(long seq, Integer ownerId, String primaryId, int membershipLevel) {

    /** The fixed {@code event} discriminator every owner-created event carries. */
    static final String EVENT_TYPE = "OWNER_CREATED";

    /**
     * Renders this event as a compact JSON object
     * {@code {seq, ownerId, customerCode, membershipLevel, event:'OWNER_CREATED'}}, with the current primary identifier
     * under the {@code customerCode} key. String values are JSON-escaped; a {@code null} identifier is rendered as JSON
     * {@code null}.
     *
     * @return the event serialized as a single-line JSON object
     */
    String toJson() {
        return "{"
            + "\"seq\":" + seq
            + ",\"ownerId\":" + ownerId
            + ",\"customerCode\":" + jsonString(primaryId)
            + ",\"membershipLevel\":" + membershipLevel
            + ",\"event\":" + jsonString(EVENT_TYPE)
            + "}";
    }

    private static String jsonString(String value) {
        if (value == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder(value.length() + 2);
        sb.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    }
                    else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append('"');
        return sb.toString();
    }
}
