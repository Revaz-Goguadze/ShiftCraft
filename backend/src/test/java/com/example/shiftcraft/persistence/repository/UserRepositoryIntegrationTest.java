package com.example.shiftcraft.persistence.repository;

import com.example.shiftcraft.persistence.entity.Role;
import com.example.shiftcraft.persistence.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Role staffRole;
    private Role managerRole;

    @BeforeEach
    void setUp() {
        // Create roles first
        staffRole = new Role("STAFF", "Staff member");
        managerRole = new Role("MANAGER", "Manager");
        
        entityManager.persist(staffRole);
        entityManager.persist(managerRole);
        entityManager.flush();
    }

    @Test
    void findByEmail_ExistingUser_ReturnsUser() {
        // Arrange
        User user = new User("test@example.com", "$2a$10$N.UN6inR9EGNhPpthkDcOuP6PNJz6i/Y5DUiH/rjc6ZJiX8V1z1CK", "Test", "User");
        user.setRoles(Set.of(staffRole));
        entityManager.persistAndFlush(user);

        // Act
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");

        // Assert
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
        assertThat(foundUser.get().getFirstName()).isEqualTo("Test");
        assertThat(foundUser.get().getLastName()).isEqualTo("User");
        assertThat(foundUser.get().getRoles()).hasSize(1);
        assertThat(foundUser.get().getRoles().iterator().next().getName()).isEqualTo("STAFF");
    }

    @Test
    void findByEmail_NonExistingUser_ReturnsEmpty() {
        // Act
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // Assert
        assertThat(foundUser).isEmpty();
    }

    @Test
    void findByStatus_ActiveUsers_ReturnsActiveUsers() {
        // Arrange
        User activeUser1 = new User("active1@example.com", "$2a$10$N.UN6inR9EGNhPpthkDcOuP6PNJz6i/Y5DUiH/rjc6ZJiX8V1z1CK", "Active", "User1");
        activeUser1.setStatus(User.UserStatus.ACTIVE);
        activeUser1.setRoles(Set.of(staffRole));
        
        User activeUser2 = new User("active2@example.com", "$2a$10$N.UN6inR9EGNhPpthkDcOuP6PNJz6i/Y5DUiH/rjc6ZJiX8V1z1CK", "Active", "User2");
        activeUser2.setStatus(User.UserStatus.ACTIVE);
        activeUser2.setRoles(Set.of(staffRole));
        
        User inactiveUser = new User("inactive@example.com", "$2a$10$N.UN6inR9EGNhPpthkDcOuP6PNJz6i/Y5DUiH/rjc6ZJiX8V1z1CK", "Inactive", "User");
        inactiveUser.setStatus(User.UserStatus.INACTIVE);
        inactiveUser.setRoles(Set.of(staffRole));

        entityManager.persist(activeUser1);
        entityManager.persist(activeUser2);
        entityManager.persist(inactiveUser);
        entityManager.flush();

        // Act
        List<User> activeUsers = userRepository.findByStatus(User.UserStatus.ACTIVE);

        // Assert
        assertThat(activeUsers).hasSize(2);
        assertThat(activeUsers)
            .extracting(User::getEmail)
            .containsExactlyInAnyOrder("active1@example.com", "active2@example.com");
    }

    @Test
    void findByRoleName_UsersWithRole_ReturnsFilteredUsers() {
        // Arrange
        User manager = new User("manager@example.com", "$2a$10$N.UN6inR9EGNhPpthkDcOuP6PNJz6i/Y5DUiH/rjc6ZJiX8V1z1CK", "Manager", "User");
        manager.setRoles(Set.of(managerRole));
        
        User staff1 = new User("staff1@example.com", "$2a$10$N.UN6inR9EGNhPpthkDcOuP6PNJz6i/Y5DUiH/rjc6ZJiX8V1z1CK", "Staff", "User1");
        staff1.setRoles(Set.of(staffRole));
        
        User staff2 = new User("staff2@example.com", "$2a$10$N.UN6inR9EGNhPpthkDcOuP6PNJz6i/Y5DUiH/rjc6ZJiX8V1z1CK", "Staff", "User2");
        staff2.setRoles(Set.of(staffRole));

        entityManager.persist(manager);
        entityManager.persist(staff1);
        entityManager.persist(staff2);
        entityManager.flush();

        // Act
        List<User> staffUsers = userRepository.findByRoleName("STAFF");
        List<User> managerUsers = userRepository.findByRoleName("MANAGER");

        // Assert
        assertThat(staffUsers).hasSize(2);
        assertThat(staffUsers)
            .extracting(User::getEmail)
            .containsExactlyInAnyOrder("staff1@example.com", "staff2@example.com");
            
        assertThat(managerUsers).hasSize(1);
        assertThat(managerUsers.get(0).getEmail()).isEqualTo("manager@example.com");
    }

    @Test
    void countActiveUsers_CountsOnlyActiveUsers() {
        // Arrange
        User activeUser1 = new User("active1@example.com", "$2a$10$N.UN6inR9EGNhPpthkDcOuP6PNJz6i/Y5DUiH/rjc6ZJiX8V1z1CK", "Active", "User1");
        activeUser1.setStatus(User.UserStatus.ACTIVE);
        activeUser1.setRoles(Set.of(staffRole));
        
        User activeUser2 = new User("active2@example.com", "$2a$10$N.UN6inR9EGNhPpthkDcOuP6PNJz6i/Y5DUiH/rjc6ZJiX8V1z1CK", "Active", "User2");
        activeUser2.setStatus(User.UserStatus.ACTIVE);
        activeUser2.setRoles(Set.of(staffRole));
        
        User inactiveUser = new User("inactive@example.com", "$2a$10$N.UN6inR9EGNhPpthkDcOuP6PNJz6i/Y5DUiH/rjc6ZJiX8V1z1CK", "Inactive", "User");
        inactiveUser.setStatus(User.UserStatus.INACTIVE);
        inactiveUser.setRoles(Set.of(staffRole));
        
        User suspendedUser = new User("suspended@example.com", "$2a$10$N.UN6inR9EGNhPpthkDcOuP6PNJz6i/Y5DUiH/rjc6ZJiX8V1z1CK", "Suspended", "User");
        suspendedUser.setStatus(User.UserStatus.SUSPENDED);
        suspendedUser.setRoles(Set.of(staffRole));

        entityManager.persist(activeUser1);
        entityManager.persist(activeUser2);
        entityManager.persist(inactiveUser);
        entityManager.persist(suspendedUser);
        entityManager.flush();

        // Act
        long activeCount = userRepository.countActiveUsers();

        // Assert
        assertThat(activeCount).isEqualTo(2);
    }

    @Test
    void existsByEmail_ExistingEmail_ReturnsTrue() {
        // Arrange
        User user = new User("existing@example.com", "$2a$10$N.UN6inR9EGNhPpthkDcOuP6PNJz6i/Y5DUiH/rjc6ZJiX8V1z1CK", "Existing", "User");
        user.setRoles(Set.of(staffRole));
        entityManager.persistAndFlush(user);

        // Act
        boolean exists = userRepository.existsByEmail("existing@example.com");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_NonExistingEmail_ReturnsFalse() {
        // Act
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void saveUser_WithRoles_PersistsCorrectly() {
        // Arrange
        User user = new User("newuser@example.com", "$2a$10$N.UN6inR9EGNhPpthkDcOuP6PNJz6i/Y5DUiH/rjc6ZJiX8V1z1CK", "New", "User");
        user.setRoles(Set.of(staffRole, managerRole)); // User with multiple roles

        // Act
        User savedUser = userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        // Retrieve from database
        Optional<User> retrievedUser = userRepository.findById(savedUser.getId());

        // Assert
        assertThat(retrievedUser).isPresent();
        User user1 = retrievedUser.get();
        assertThat(user1.getEmail()).isEqualTo("newuser@example.com");
        assertThat(user1.getRoles()).hasSize(2);
        assertThat(user1.getRoles())
            .extracting(Role::getName)
            .containsExactlyInAnyOrder("STAFF", "MANAGER");
    }

    @Test
    void deleteUser_RemovesUserButKeepsRoles() {
        // Arrange
        User user = new User("delete@example.com", "$2a$10$N.UN6inR9EGNhPpthkDcOuP6PNJz6i/Y5DUiH/rjc6ZJiX8V1z1CK", "Delete", "Me");
        user.setRoles(Set.of(staffRole));
        User savedUser = entityManager.persistAndFlush(user);

        // Act
        userRepository.delete(savedUser);
        entityManager.flush();

        // Assert
        Optional<User> deletedUser = userRepository.findById(savedUser.getId());
        assertThat(deletedUser).isEmpty();
        
        // Roles should still exist
        Optional<Role> role = roleRepository.findById(staffRole.getId());
        assertThat(role).isPresent();
    }
}