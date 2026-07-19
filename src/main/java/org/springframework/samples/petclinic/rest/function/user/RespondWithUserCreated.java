package org.springframework.samples.petclinic.rest.function.user;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.UserMapper;
import org.springframework.samples.petclinic.model.User;
import org.springframework.samples.petclinic.rest.dto.UserDto;

public class RespondWithUserCreated {

    public void service(@Val User user, UserMapper userMapper, ObjectResponse<ResponseEntity<UserDto>> response) {
        response.send(ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toUserDto(user)));
    }
}
