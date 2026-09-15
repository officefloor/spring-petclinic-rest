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
package org.springframework.samples.petclinic.rest;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

/**
 * Emits the immutable {@link OwnerCreatedEvent} to the dedicated {@code AUDIT} logger as JSON,
 * assigning each created owner the next value of a monotonically increasing sequence.
 */
@Component
public class OwnerCreatedEventEmitter {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private final AtomicLong sequence = new AtomicLong();

    private final ObjectMapper objectMapper;

    public OwnerCreatedEventEmitter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Emit the structured {@code OWNER_CREATED} event for a freshly persisted owner, stamping it
     * with the next sequence number.
     *
     * @param owner the persisted owner
     */
    public void emit(Owner owner) {
        OwnerCreatedEvent event = new OwnerCreatedEvent(sequence.incrementAndGet(), owner);
        AUDIT.info(objectMapper.writeValueAsString(event));
    }
}
