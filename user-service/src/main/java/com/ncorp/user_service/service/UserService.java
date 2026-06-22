package com.ncorp.user_service.service;

import com.ncorp.user_service.dto.UserDto;
import com.ncorp.user_service.entity.User;
import com.ncorp.user_service.exception.UserNotFoundException;
import com.ncorp.user_service.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Optional;

@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public UserDto createUser(@RequestBody UserDto userDto){
        final User createdUser =  User.builder()
                .name(userDto.getName())
                .surname(userDto.getSurname())
                .email(userDto.getEmail())
                .address(userDto.getAddress())
                .alerting(userDto.isAlerting())
                .energyAlertingThreshold(userDto.getEnergyAlertingThreshold())
                .build();

        final User saved = userRepository.save(createdUser);

        return toDto(saved);
    }


    private UserDto toDto(User user){
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .surname(user.getSurname())
                .email(user.getEmail())
                .address(user.getAddress())
                .alerting(user.isAlerting())
                .energyAlertingThreshold(user.getEnergyAlertingThreshold())
                .build();
    }

    public UserDto getUserById(Long id) {
        final Optional<User> user = userRepository.findById(id);
        return user.map(this::toDto).orElse(null);
    }

    public void updateUser(Long id, UserDto userDto) {
        final Optional<User> user = userRepository.findById(id);
        if(user.isEmpty()) {
            throw new UserNotFoundException("User not found");
        }
        user.get().setName(userDto.getName());
        user.get().setSurname(userDto.getSurname());
        user.get().setEmail(userDto.getEmail());
        user.get().setAddress(userDto.getAddress());
        user.get().setAlerting(userDto.isAlerting());
        user.get().setEnergyAlertingThreshold(userDto.getEnergyAlertingThreshold());
        userRepository.save(user.get());
    }

    public void deleteUser(Long id) {
        try{
            userRepository.deleteById(id);
        }catch (UserNotFoundException e) {
            throw new UserNotFoundException("User not found");
        }
    }
}
