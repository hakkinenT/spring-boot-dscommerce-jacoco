package com.devsuperior.dscommerce.services;

import com.devsuperior.dscommerce.dto.UserDTO;
import com.devsuperior.dscommerce.entities.User;
import com.devsuperior.dscommerce.projections.UserDetailsProjection;
import com.devsuperior.dscommerce.repositories.UserRepository;
import com.devsuperior.dscommerce.tests.UserDetailsFactory;
import com.devsuperior.dscommerce.tests.UserFactory;
import com.devsuperior.dscommerce.utils.CustomUserUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
public class UserServiceTests {
    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomUserUtil customUserUtil;

    private User userClient;

    private User userAdmin;

    private UserDTO userDTO;

    private String existingUsername;

    private String nonExistingUsername;

    private List<UserDetailsProjection> userDetails;

    @BeforeEach
    void setUp() throws Exception{
        existingUsername = "maria@gmail.com";
        nonExistingUsername = "user@gmail.com";

        userClient = UserFactory.createCustomClientUser(1L, existingUsername);
        userDetails = UserDetailsFactory.createCustomAdminUser(existingUsername);

        Mockito.when(userRepository.searchUserAndRolesByEmail(existingUsername)).thenReturn(userDetails);
        Mockito.when(userRepository.searchUserAndRolesByEmail(nonExistingUsername)).thenReturn(new ArrayList<>());

        Mockito.when(userRepository.findByEmail(existingUsername)).thenReturn(Optional.of(userClient));
        Mockito.when(userRepository.findByEmail(nonExistingUsername)).thenReturn(Optional.empty());
    }

    @Test
    public void loadByUsernameShouldReturnUserDetailsWhenUserExist(){
        UserDetails result = userService.loadUserByUsername(existingUsername);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getUsername(), existingUsername);
    }

    @Test
    public void loadByUsernameShouldThrowUserNotFoundExceptionWhenUserDoesNotExist(){
        Assertions.assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername(nonExistingUsername);
        });
    }

    @Test
    public void authenticatedShouldReturnUserWhenUserExists(){
        Mockito.when(customUserUtil.getLoggedUsername()).thenReturn(existingUsername);

        User result = userService.authenticated();

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getUsername(), existingUsername);
    }

    @Test
    public void authenticatedShouldThrowUsernameNotFoundExceptionUserWhenUserDoesNotExist(){
        String loggedUsername = Mockito.doThrow(ClassCastException.class).when(customUserUtil).getLoggedUsername();


        Assertions.assertThrows(UsernameNotFoundException.class, () -> {
            userService.authenticated();
        });
    }

    @Test
    public void getMeShouldReturnUserDTOWhenUserAuthenticated(){
        UserService spyUserService = Mockito.spy(userService);
        Mockito.doReturn(userClient).when(spyUserService).authenticated();

        UserDTO result = spyUserService.getMe();

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getEmail(), existingUsername);
    }

    @Test
    public void getMeShouldThrowUsernameNotFoundExceptionWhenUserNotAuthenticated(){
        UserService spyUserService = Mockito.spy(userService);
        Mockito.doThrow(UsernameNotFoundException.class).when(spyUserService).authenticated();

        Assertions.assertThrows(UsernameNotFoundException.class, () -> {
            UserDTO result = spyUserService.getMe();
        });
    }
}
