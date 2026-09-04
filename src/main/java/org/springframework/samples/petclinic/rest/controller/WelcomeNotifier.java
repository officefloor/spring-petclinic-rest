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

package org.springframework.samples.petclinic.rest.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

/**
 * Owns the welcome-notification concern: the single place that enqueues the welcome
 * notification for a successfully created {@link Owner}.
 *
 * <p>Keeping it here means the registrar is left holding only the create pipeline, and the
 * notification is emitted from one place, in one form, on the dedicated {@code NOTIFY} logger
 * — the queue onto which welcome notifications are enqueued.
 */
@Component
public class WelcomeNotifier {

    /** Dedicated notification queue; welcome notifications are enqueued here on successful create. */
    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    /**
     * Enqueue the welcome notification for an owner that has just been created: emit the
     * notification naming the owner's id and member id. Called once, after the owner is
     * persisted, with the owner in its final stored form.
     *
     * @param owner the freshly persisted owner
     */
    public void welcome(Owner owner) {
        NOTIFY.info("welcome ownerId={} memberId={}", owner.getId(), owner.getMemberId());
    }
}
