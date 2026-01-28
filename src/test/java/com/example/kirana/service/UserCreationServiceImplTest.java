package com.example.kirana.service;

import com.example.kirana.dto.CreateUserRequest;
import com.example.kirana.dto.CreateUserResponse;
import com.example.kirana.model.mongo.*;
import com.example.kirana.model.mongo.UserRole;
import com.example.kirana.repository.mongo.*;
import com.example.kirana.repository.mongo.UserRolesRepository;
import com.example.kirana.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCreationServiceImplTest {

    @InjectMocks
    private UserServiceImpl userCreationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RolesRepository roleRepository;
    @Mock
    private UserRolesRepository userRoleRepository;
    @Mock
    private StoreRepository storeRepository;



    // --------------------------------------------------
    // SUCCESSFUL USER CREATION
    // --------------------------------------------------

    @Test
    void createUser_success() {

        // GIVEN
        CreateUserRequest request = new CreateUserRequest();
        request.setUserName("john_doe");
        request.setPassword("password123");
        request.setRole("USER");
        request.setStoreId("STORE_1");

        when(userRepository.findByUserName("john_doe"))
                .thenReturn(Optional.empty());

        Role role = new Role();
        role.setRoleId("ROLE_USER");
        role.setRoleName("USER");

        when(roleRepository.findByRoleName("USER"))
                .thenReturn(Optional.of(role));

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");

        when(userRepository.save(any(User.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        when(userRoleRepository.save(any(UserRole.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(storeRepository.existsById("STORE_1"))
                .thenReturn(true);

        // WHEN
        CreateUserResponse response = userCreationService.createUser(request);

        // THEN
        assertNotNull(response);
        assertEquals("john_doe", response.getUserName());
        assertEquals("USER", response.getRole());
        assertEquals("STORE_1", response.getStoreId());

        // verify user saved
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("john_doe", savedUser.getUserName());
        assertEquals("encodedPassword", savedUser.getPassword());

        // verify mapping saved
        ArgumentCaptor<UserRole> roleCaptor = ArgumentCaptor.forClass(UserRole.class);
        verify(userRoleRepository).save(roleCaptor.capture());

        UserRole mapping = roleCaptor.getValue();
        assertEquals("ROLE_USER", mapping.getRoleId());
        assertEquals("STORE_1", mapping.getStoreId());
        assertNotNull(mapping.getUserId());
    }


    // --------------------------------------------------
    //  DUPLICATE USERNAME
    // --------------------------------------------------
    @Test
    void createUser_usernameAlreadyExists_shouldFail() {

        CreateUserRequest request = validRequest();

        User existingUser = new User();
        existingUser.setUserName("john_doe");
        existingUser.setPassword("encoded");
        existingUser.setCreatedAt(LocalDateTime.now());

        when(userRepository.findByUserName("john_doe"))
                .thenReturn(Optional.of(existingUser));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> userCreationService.createUser(request)
        );

        assertEquals("Username already exists: john_doe", ex.getMessage());

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
        verify(userRoleRepository, never()).save(any());
    }

    // --------------------------------------------------
    // PASSWORD ENCODING FAILURE
    // --------------------------------------------------
    @Test
    void createUser_passwordEncodingFails() {

        CreateUserRequest request = validRequest();

        when(userRepository.findByUserName("john_doe"))
                .thenReturn(Optional.empty());

       // when(storeRepository.existsById("STORE_1"))
          //      .thenReturn(true);

        Role role = new Role();
        role.setRoleId("ROLE_USER");
        role.setRoleName("USER");

        when(roleRepository.findByRoleName("USER"))
                .thenReturn(Optional.of(role));

        when(passwordEncoder.encode(anyString()))
                .thenThrow(new RuntimeException("Encoding failed"));

        assertThrows(RuntimeException.class,
                () -> userCreationService.createUser(request));

        verify(userRepository, never()).save(any());
        verify(userRoleRepository, never()).save(any());
    }


    // --------------------------------------------------
    // MISSING USERNAME
    // --------------------------------------------------
    @Test
    void createUser_missingUsername_shouldFail() {

        CreateUserRequest request = validRequest();
        request.setUserName(null);

        assertThrows(
                RuntimeException.class,
                () -> userCreationService.createUser(request)
        );

        verifyNoInteractions(userRepository, passwordEncoder, userRoleRepository);
    }

    // --------------------------------------------------
    // MISSING PASSWORD
    // --------------------------------------------------
    @Test
    void createUser_missingPassword_shouldFail() {

        CreateUserRequest request = validRequest();
        request.setPassword(null);

        assertThrows(
                RuntimeException.class,
                () -> userCreationService.createUser(request)
        );

        verifyNoInteractions(userRepository, passwordEncoder, userRoleRepository);
    }

    // --------------------------------------------------
    //  MISSING ROLE
    // --------------------------------------------------
    @Test
    void createUser_missingRole_shouldFail() {

        CreateUserRequest request = validRequest();
        request.setRole(null);

        assertThrows(
                RuntimeException.class,
                () -> userCreationService.createUser(request)
        );

        verifyNoInteractions(userRepository, passwordEncoder, userRoleRepository);
    }

    // --------------------------------------------------
    // 7️MISSING STORE ID
    // --------------------------------------------------
    @Test
    void createUser_missingStoreId_shouldFail() {

        CreateUserRequest request = validRequest();
        request.setStoreId(null);

        assertThrows(
                RuntimeException.class,
                () -> userCreationService.createUser(request)
        );

        verify(userRepository, never()).save(any());
        verify(userRoleRepository, never()).save(any());

    }




    // --------------------------------------------------
    //TEST DATA BUILDER
    // --------------------------------------------------
    private CreateUserRequest validRequest() {
        CreateUserRequest req = new CreateUserRequest();
        req.setUserName("john_doe");
        req.setPassword("password123");
        req.setRole("USER");
        req.setStoreId("STORE_1");
        return req;
    }
    private Role mockRole() {
        Role role = new Role();
        role.setRoleId("ROLE_USER");
        role.setRoleName("USER");
        return role;
    }

}
