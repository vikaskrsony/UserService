package com.vikas.UserService.Mapper;

import com.vikas.UserService.DTO.UserDTO;
import com.vikas.UserService.Models.User;

public class UserEntityDTOMapper {
    public static UserDTO getUserDTOFromUserEntity(User user){
        UserDTO userDto = new UserDTO();
        userDto.setEmail(user.getEmail());
        userDto.setRoles(user.getRoles());
        return userDto;
    }
}
