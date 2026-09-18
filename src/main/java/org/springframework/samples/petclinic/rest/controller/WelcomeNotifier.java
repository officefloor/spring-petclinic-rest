/*
 * Copyright 2016 the original author or authors.
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
 * Enqueues the welcome notification for a newly created owner.
 * <p>
 * On a successful create a single line is written to the dedicated {@code NOTIFY}
 * logger carrying the newly assigned owner id and its member id, so the welcome
 * side-effect can be observed independently of the REST response.
 */
@Component
public class WelcomeNotifier {

    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    /**
     * Emits the welcome notification for the just-created {@code owner}. Call this only
     * after the owner has been saved so its id and member id are populated.
     *
     * @param owner the owner that was created
     */
    public void enqueueWelcome(Owner owner) {
        NOTIFY.info("Welcome notification enqueued: id={} memberId={}",
            owner.getId(), owner.getMemberId());
    }
}
