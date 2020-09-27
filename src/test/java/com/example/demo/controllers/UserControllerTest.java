package com.example.demo.controllers;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class UserControllerTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private CartRepository cartRepository;

  @Mock
  private BCryptPasswordEncoder bCryptPasswordEncoder;

  @InjectMocks
  private UserController userController;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void findById_FOUND() {
    User user = createUser();
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    ResponseEntity<User> response = userController.findById(1L);
    assertNotNull(response);
    assertEquals(200, response.getStatusCodeValue());

    User currentUser = response.getBody();
    assertNotNull(currentUser);
    assertEquals(1, currentUser.getId());
    assertEquals("testUsername", currentUser.getUsername());
    assertEquals("thisIsHashed", currentUser.getPassword());

    verify(userRepository, times(1)).findById(anyLong());
  }

  @Test
  public void findById_NOT_FOUND() {
    User user = createUser();
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    ResponseEntity<User> response = userController.findById(2L);
    assertNotNull(response);
    assertNull(response.getBody());
    assertEquals(404, response.getStatusCodeValue());

    verify(userRepository, times(1)).findById(anyLong());
  }

  @Test
  public void findByUserName_FOUND() {
    User user = createUser();
    when(userRepository.findByUsername("testUsername")).thenReturn(user);

    ResponseEntity<User> response = userController.findByUserName("testUsername");
    assertNotNull(response);
    assertEquals(200, response.getStatusCodeValue());

    User currentUser = response.getBody();
    assertNotNull(currentUser);
    assertEquals(1, currentUser.getId());
    assertEquals("testUsername", currentUser.getUsername());
    assertEquals("thisIsHashed", currentUser.getPassword());

    verify(userRepository, times(1)).findByUsername(anyString());
  }

  @Test
  public void findByUserName_NOT_FOUND() {
    User user = createUser();
    when(userRepository.findByUsername("testUsername")).thenReturn(user);

    ResponseEntity<User> response = userController.findByUserName("testDifferentUsername");
    assertNotNull(response);
    assertNull(response.getBody());
    assertEquals(404, response.getStatusCodeValue());

    verify(userRepository, times(1)).findByUsername(anyString());
  }

  @Test
  public void createUser_SUCCESS() {
    when(bCryptPasswordEncoder.encode("testPassword")).thenReturn("thisIsHashed");

    CreateUserRequest request = createUserRequest();

    ResponseEntity<User> response = userController.createUser(request);
    assertNotNull(response);
    assertEquals(200, response.getStatusCodeValue());

    User user = response.getBody();
    assertNotNull(user);
    assertEquals(0, user.getId());
    assertEquals("testUsername", user.getUsername());
    assertEquals("thisIsHashed", user.getPassword());

    verify(bCryptPasswordEncoder, times(1)).encode(anyString());
    verify(userRepository, times(1)).save(isA(User.class));
    verify(cartRepository, times(1)).save(isA(Cart.class));
  }

  @Test
  public void createUser_FAIL_PASSWORD_TOO_SHORT() {
    CreateUserRequest request = createUserRequest();
    request.setPassword("testP");
    request.setConfirmPassword("testP");

    ResponseEntity<User> response = userController.createUser(request);
    assertNotNull(response);
    assertNull(response.getBody());
    assertEquals(400, response.getStatusCodeValue());

    verify(bCryptPasswordEncoder, times(0)).encode(anyString());
    verify(userRepository, times(0)).save(isA(User.class));
    verify(cartRepository, times(1)).save(isA(Cart.class));
  }

  @Test
  public void createUser_FAIL_PASSWORDS_DO_NOT_MATCH() {
    CreateUserRequest request = createUserRequest();
    request.setPassword("testPassword");
    request.setConfirmPassword("testPass");

    ResponseEntity<User> response = userController.createUser(request);
    assertNotNull(response);
    assertNull(response.getBody());
    assertEquals(400, response.getStatusCodeValue());

    verify(bCryptPasswordEncoder, times(0)).encode(anyString());
    verify(userRepository, times(0)).save(isA(User.class));
    verify(cartRepository, times(1)).save(isA(Cart.class));
  }

  private User createUser() {
    User user = new User();
    user.setId(1L);
    user.setUsername("testUsername");
    user.setPassword("thisIsHashed");
    return user;
  }

  private CreateUserRequest createUserRequest() {
    CreateUserRequest request = new CreateUserRequest();
    request.setUsername("testUsername");
    request.setPassword("testPassword");
    request.setConfirmPassword("testPassword");
    return request;
  }

}
