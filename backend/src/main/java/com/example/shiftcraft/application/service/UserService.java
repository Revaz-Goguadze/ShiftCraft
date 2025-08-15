package com.example.shiftcraft.application.service;

import com.example.shiftcraft.persistence.entity.Role;
import com.example.shiftcraft.persistence.entity.User;
import com.example.shiftcraft.persistence.repository.RoleRepository;
import com.example.shiftcraft.persistence.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * Create a new user
     */
    public User createUser(String email, String password, String firstName, String lastName, Set<String> roleNames) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("User with email " + email + " already exists");
        }
        
        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(email, encodedPassword, firstName, lastName);
        
        // Add roles
        for (String roleName : roleNames) {
            Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
            user.getRoles().add(role);
        }
        
        return userRepository.save(user);
    }
    
    /**
     * Find user by email
     */
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    /**
     * Find user by id
     */
    @Transactional(readOnly = true)
    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }
    
    /**
     * Get all users
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * Get users by role
     */
    @Transactional(readOnly = true)
    public List<User> getUsersByRole(String roleName) {
        return userRepository.findByRoleName(roleName);
    }
    
    /**
     * Get active users
     */
    @Transactional(readOnly = true)
    public List<User> getActiveUsers() {
        return userRepository.findByStatus(User.UserStatus.ACTIVE);
    }
    
    /**
     * Update user status
     */
    public User updateUserStatus(String userId, User.UserStatus status) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        user.setStatus(status);
        return userRepository.save(user);
    }
    
    /**
     * Add role to user
     */
    public User addRoleToUser(String userId, String roleName) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
        
        user.getRoles().add(role);
        return userRepository.save(user);
    }
    
    /**
     * Check if user has role
     */
    @Transactional(readOnly = true)
    public boolean userHasRole(String userId, String roleName) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }
        
        return user.getRoles().stream()
            .anyMatch(role -> role.getName().equals(roleName));
    }
    
    /**
     * Get managers (users with MANAGER role)
     */
    @Transactional(readOnly = true)
    public List<User> getManagers() {
        return userRepository.findByRoleName("MANAGER");
    }
    
    /**
     * Get staff (users with STAFF role)
     */
    @Transactional(readOnly = true)
    public List<User> getStaff() {
        return userRepository.findByRoleName("STAFF");
    }
}