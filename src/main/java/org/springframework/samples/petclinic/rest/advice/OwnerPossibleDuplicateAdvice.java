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

package org.springframework.samples.petclinic.rest.advice;

import java.util.Comparator;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Flags a returned owner as a possible (soft) duplicate of an earlier owner that shares its last
 * name and postcode but has a different telephone; a whole-key match is a hard duplicate and never
 * gets created. Kept as its own small advice so this rule stays a self-contained unit rather than
 * growing the controller, the mapper or another handler.
 */
@ControllerAdvice
public class OwnerPossibleDuplicateAdvice implements ResponseBodyAdvice<Object> {

    private final ClinicService clinicService;

    public OwnerPossibleDuplicateAdvice(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof OwnerDto owner) {
            Integer match = clinicService.findAllOwners().stream()
                .filter(other -> owner.getId() != null && other.getId() < owner.getId())
                .filter(other -> softMatch(owner, other))
                .map(Owner::getId)
                .min(Comparator.naturalOrder())
                .orElse(null);
            owner.setPossibleDuplicate(match != null);
            owner.setPossibleDuplicateOf(match);
        }
        return body;
    }

    private static boolean softMatch(OwnerDto owner, Owner other) {
        return owner.getPostcode() != null
            && owner.getPostcode().equals(other.getPostcode())
            && owner.getLastName() != null
            && owner.getLastName().equalsIgnoreCase(other.getLastName())
            && !OwnerIdentityKey.telephone(owner.getTelephone()).equals(OwnerIdentityKey.telephone(other.getTelephone()));
    }
}
