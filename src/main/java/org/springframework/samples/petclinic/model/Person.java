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
package org.springframework.samples.petclinic.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import jakarta.validation.constraints.NotEmpty;

/**
 * Simple JavaBean domain object representing an person.
 *
 * @author Ken Krebs
 */
@MappedSuperclass
public class Person extends BaseEntity {

    @Column(name = "first_name")
    @NotEmpty
    protected String firstName;

    @Column(name = "last_name")
    @NotEmpty
    protected String lastName;

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Return the person's initials as the upper-cased first letters of the first
     * and last name, dot-separated with a trailing dot, e.g. {@code "J.S."}.
     */
    public String getInitials() {
        StringBuilder initials = new StringBuilder();
        if (this.firstName != null && !this.firstName.isEmpty()) {
            initials.append(Character.toUpperCase(this.firstName.charAt(0))).append('.');
        }
        if (this.lastName != null && !this.lastName.isEmpty()) {
            initials.append(Character.toUpperCase(this.lastName.charAt(0))).append('.');
        }
        return initials.toString();
    }


}
