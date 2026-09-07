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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

/**
 * Enqueues the welcome notification for a freshly-created owner. The welcome-notify
 * concern is gathered here, off the {@link OwnerRestControllerV1 controller}, so the
 * controller stays a thin entry point and the notification's format — and the owner
 * attributes it carries — lives in one place, free to grow independently of the
 * request handling and of the {@link OwnerCreationAuditor audit trail}.
 *
 * <p>The notification is emitted to the dedicated {@code NOTIFY} logger, the queue a
 * downstream welcome-message consumer drains, and carries the owner's id alongside its
 * assigned {@link Owner#getMemberId() member id} so the recipient can be resolved.
 */
@Component
public class WelcomeNotifier {

    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    /**
     * Enqueue the welcome notification for a freshly-saved owner, emitting a line to the
     * {@code NOTIFY} logger that carries the owner's id and assigned member id.
     *
     * @param owner the owner that has just been created and saved
     */
    public void enqueueWelcome(Owner owner) {
        NOTIFY.info("welcome owner id={} memberId={}", owner.getId(), owner.getMemberId());
    }
}
