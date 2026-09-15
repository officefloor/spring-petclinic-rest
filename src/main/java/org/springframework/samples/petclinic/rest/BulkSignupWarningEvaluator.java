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

import org.springframework.stereotype.Component;

/**
 * Decides whether an owner being created should carry a bulk-signup warning. The warning
 * flags that an unusually high number of owners have already been registered on the same
 * day, which may indicate a bulk sign-up.
 */
@Component
public class BulkSignupWarningEvaluator {

    /** Number of owners already registered on a day above which a new owner is flagged. */
    public static final long BULK_SIGNUP_THRESHOLD = 80;

    /**
     * Whether an owner joining a day that already holds {@code ownersAlreadyRegistered}
     * owners should be flagged as a bulk signup.
     *
     * @param ownersAlreadyRegistered owners already registered for the day, excluding the new one
     * @return {@code true} when more than {@link #BULK_SIGNUP_THRESHOLD} owners already exist
     *         for the day, otherwise {@code false}
     */
    public boolean isBulkSignup(long ownersAlreadyRegistered) {
        return ownersAlreadyRegistered > BULK_SIGNUP_THRESHOLD;
    }
}
