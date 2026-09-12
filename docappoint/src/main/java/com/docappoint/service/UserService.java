package com.docappoint.service;

import com.docappoint.dto.UserResponse;
import com.docappoint.entity.User;
import com.docappoint.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse getCurrentUserDetails(User currentUser) {
        if (currentUser == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        return new UserResponse(currentUser.getId(), currentUser.getName());
    }

    public UserResponse getUserById(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        return new UserResponse(user.getId(), user.getName());
    }
}
