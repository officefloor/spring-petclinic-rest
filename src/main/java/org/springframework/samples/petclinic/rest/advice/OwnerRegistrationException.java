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

package org.springframework.samples.petclinic.rest.advice;

import org.springframework.http.HttpStatus;

/**
 * Signals that a create rule refused to register an owner. This is the one way the create
 * pipeline reports a rejection: rather than constructing an HTTP response inline, each rule
 * names its refusal as a {@link Reason} and throws, and {@link ExceptionControllerAdvice#handleOwnerRegistrationException}
 * turns it into the response the endpoint returns.
 *
 * <p>The catalogue of refusals lives on {@link Reason}, which pairs every business cause with
 * the HTTP status it reports, so the mapping is defined in one place instead of being spread
 * across the rejection sites.
 */
public class OwnerRegistrationException extends RuntimeException {

    /**
     * The business reasons a create can be refused, each paired with the HTTP status the
     * endpoint reports for it. Adding a rejection to the pipeline means adding a constant here,
     * so every refusal is named and its status is defined in a single place.
     */
    public enum Reason {

        /** A supplied registration date is later than the server's current date. */
        REGISTRATION_DATE_IN_FUTURE(HttpStatus.BAD_REQUEST, "The supplied registration date is in the future"),

        /** The per-day create quota for the registration date has already been reached. */
        DAILY_CREATE_LIMIT_REACHED(HttpStatus.TOO_MANY_REQUESTS, "The daily create limit for this registration date has been reached"),

        /** The owner supplies no usable address in either the structured or the flat form. */
        ADDRESS_MISSING(HttpStatus.BAD_REQUEST, "The owner has no usable address"),

        /** The telephone cannot be normalized or has an invalid national number length. */
        TELEPHONE_INVALID(HttpStatus.BAD_REQUEST, "The supplied telephone number is invalid"),

        /** A supplied email is not a syntactically valid address. */
        EMAIL_INVALID(HttpStatus.BAD_REQUEST, "The supplied email address is not valid"),

        /** A supplied email belongs to a disposable-address domain. */
        EMAIL_DISPOSABLE(HttpStatus.BAD_REQUEST, "The supplied email address belongs to a disposable-address domain"),

        /** A supplied postcode is malformed or out of range for the owner's city. */
        POSTCODE_INVALID(HttpStatus.BAD_REQUEST, "The supplied postcode is malformed or out of range for the owner's city"),

        /** The candidate collides with an existing owner under the identity rule. */
        DUPLICATE_OWNER(HttpStatus.CONFLICT, "An owner with the same identity already exists"),

        /** The owner's city has already reached its capacity ceiling. */
        CITY_CAPACITY_REACHED(HttpStatus.CONFLICT, "The owner's city has reached its capacity ceiling");

        private final HttpStatus status;

        private final String detail;

        Reason(HttpStatus status, String detail) {
            this.status = status;
            this.detail = detail;
        }

        /** The HTTP status the endpoint reports for this rejection. */
        public HttpStatus getStatus() {
            return status;
        }

        /** A human-readable explanation of this rejection, used as the problem detail. */
        public String getDetail() {
            return detail;
        }
    }

    private final Reason reason;

    public OwnerRegistrationException(Reason reason) {
        this.reason = reason;
    }

    /** The business reason this create was refused. */
    public Reason getReason() {
        return reason;
    }

    /** The HTTP status the endpoint reports for this rejection. */
    public HttpStatus getStatus() {
        return reason.getStatus();
    }
}
