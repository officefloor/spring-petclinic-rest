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

package org.springframework.samples.petclinic.audit;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

/**
 * Publishes immutable structured audit events to the dedicated {@code AUDIT} logger.
 *
 * <p>Each owner create is stamped with a {@code seq} drawn from a single process-wide
 * counter, so the sequence increases monotonically across all creates.
 */
@Component
public class OwnerAuditEventPublisher {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private final ObjectMapper objectMapper;

    private final AtomicLong sequence = new AtomicLong();

    public OwnerAuditEventPublisher(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Emit an {@code OWNER_CREATED} event for a freshly created owner. {@code membershipLevel}
     * is supplied by the caller rather than derived here; see {@link OwnerCreatedEvent#of}.
     */
    public void ownerCreated(Owner owner, Integer membershipLevel) {
        OwnerCreatedEvent event = OwnerCreatedEvent.of(sequence.incrementAndGet(), owner, membershipLevel);
        AUDIT.info(objectMapper.writeValueAsString(event));
    }
}
