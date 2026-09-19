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

package org.springframework.samples.petclinic.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

/**
 * Enqueues member-facing notifications for owner lifecycle events on the dedicated
 * {@code NOTIFY} logger, keeping notification concerns out of the business logic that
 * triggers them.
 */
@Component
public class WelcomeNotifier {

    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    /**
     * Enqueue a welcome notification for a freshly created owner, carrying their id and
     * {@code memberId} so the notification can be addressed to the new member.
     *
     * @param owner the persisted owner (with its generated id and member id) to welcome
     */
    public void welcome(Owner owner) {
        NOTIFY.info("Welcome notification enqueued: id={} memberId={}", owner.getId(), owner.getMemberId());
    }
}
