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
import java.util.Objects;

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
 * Flags a returned owner as a suspected duplicate (soft match) of an earlier owner. Now that the
 * identity key includes the telephone, two owners with the same {@code soundex(lastName)} and
 * postcode but different telephones share no identity key and are both created; the later one is a
 * soft match of the earlier. Kept as its own small advice so this rule stays a self-contained unit.
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
            Owner match = softMatch(owner);
            owner.setPossibleDuplicate(match != null);
            owner.setPossibleDuplicateOf(match == null ? null : match.getId());
        }
        return body;
    }

    /** The earliest non-deleted owner with the same soundex(lastName) and postcode but a
     *  different identity key, or {@code null} when none exists. */
    private Owner softMatch(OwnerDto owner) {
        String soundex = OwnerSoundex.soundex(owner.getLastName());
        return clinicService.findAllOwners().stream()
            .filter(o -> !Boolean.TRUE.equals(o.getDeleted()))
            .filter(o -> owner.getId() != null && o.getId() < owner.getId())
            .filter(o -> Objects.equals(o.getPostcode(), owner.getPostcode()))
            .filter(o -> soundex.equals(OwnerSoundex.soundex(o.getLastName())))
            .filter(o -> !OwnerIdentityKey.of(o).equals(owner.getIdentityKey()))
            .min(Comparator.comparing(Owner::getId))
            .orElse(null);
    }
}
