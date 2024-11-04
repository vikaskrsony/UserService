package com.vikas.UserService.Service;

import com.vikas.UserService.DTO.UserDTO;
import com.vikas.UserService.Mapper.UserEntityDTOMapper;
import com.vikas.UserService.Models.Role;
import com.vikas.UserService.Models.User;
import com.vikas.UserService.Repository.RoleRepository;
import com.vikas.UserService.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {
    private UserRepository userRepository;
    private RoleRepository roleRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public UserDTO getUserDetails(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return null;
        }

        return UserEntityDTOMapper.getUserDTOFromUserEntity(userOptional.get());
    }

    public UserDTO setUserRoles(Long userId, List<Long> roleIds) {
        Optional<User> userOptional = userRepository.findById(userId);
        Set<Role> roles = roleRepository.findAllByIdIn(roleIds);

        if (userOptional.isEmpty()) {
            return null;
        }
        User user = userOptional.get();
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        return UserEntityDTOMapper.getUserDTOFromUserEntity(savedUser);
    }
}
