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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * Idempotent variant of {@code POST /api/owners}. Selected only when an
 * {@code Idempotency-Key} header is present (a more specific mapping than the
 * plain create), so the ordinary create path is untouched. A repeated key
 * returns the originally created owner with 200 instead of creating a duplicate.
 */
@RestController
@CrossOrigin(exposedHeaders = "errors, content-type")
@RequestMapping("/api")
public class OwnerIdempotentCreateControllerV1 {

    private final OwnerRestControllerV1 delegate;

    private final Map<String, OwnerDto> createdByKey = new ConcurrentHashMap<>();

    public OwnerIdempotentCreateControllerV1(OwnerRestControllerV1 delegate) {
        this.delegate = delegate;
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @PostMapping(value = "/owners", headers = "Idempotency-Key",
        consumes = "application/json", produces = "application/json")
    public ResponseEntity<OwnerDto> addOwner(@RequestHeader("Idempotency-Key") String key,
                                             @Valid @RequestBody OwnerFieldsDto ownerFieldsDto) {
        OwnerDto existing = createdByKey.get(key);
        if (existing != null) {
            return new ResponseEntity<>(existing, HttpStatus.OK);
        }
        ResponseEntity<OwnerDto> created = delegate.addOwner(ownerFieldsDto);
        createdByKey.put(key, created.getBody());
        return created;
    }
}
