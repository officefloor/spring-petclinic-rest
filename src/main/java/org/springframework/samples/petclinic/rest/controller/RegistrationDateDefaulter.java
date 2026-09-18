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

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

/**
 * Supplies an owner's registration date when the client did not provide one.
 * <p>
 * A registration date is optional on create; when absent it defaults to the server's
 * current date so every stored owner carries the day it was registered. An explicitly
 * supplied date is left untouched.
 */
@Component
public class RegistrationDateDefaulter {

    private final Clock clock;

    public RegistrationDateDefaulter(Clock clock) {
        this.clock = clock;
    }

    /**
     * Sets {@code owner}'s registration date to the server's current date unless one was
     * already supplied.
     *
     * @param owner the owner being created
     */
    public void applyDefault(Owner owner) {
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(LocalDate.now(clock));
        }
    }
}
