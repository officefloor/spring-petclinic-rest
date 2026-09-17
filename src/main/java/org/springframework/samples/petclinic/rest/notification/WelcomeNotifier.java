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

package org.springframework.samples.petclinic.rest.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

/**
 * Enqueues welcome notifications for newly created owners on the dedicated {@code NOTIFY}
 * logger, keeping notification side-effects out of the request-handling flow.
 */
@Component
public class WelcomeNotifier {

    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    /**
     * Enqueue a welcome notification for an owner that has just been created.
     *
     * @param owner the owner that has just been persisted
     */
    public void welcome(Owner owner) {
        NOTIFY.info("welcome owner id={} memberId={}", owner.getId(), owner.getMemberId());
    }
}
