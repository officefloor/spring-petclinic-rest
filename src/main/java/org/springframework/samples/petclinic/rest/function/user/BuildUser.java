package org.springframework.samples.petclinic.rest.function.user;

import jakarta.validation.Valid;
import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.mapper.UserMapper;
import org.springframework.samples.petclinic.model.User;
import org.springframework.samples.petclinic.rest.dto.UserDto;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

@Validated
public class BuildUser {

    public void service(@Valid @RequestBody UserDto request, UserMapper userMapper, Out<User> built) {
        built.set(userMapper.toUser(request));
    }
}
