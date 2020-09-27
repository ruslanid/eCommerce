package com.example.demo.controllers;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.ModifyCartRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CartControllerTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private ItemRepository itemRepository;

  @Mock
  private CartRepository cartRepository;

  @InjectMocks
  private CartController cartController;

  private User user;

  private Item item;

  private ModifyCartRequest request;

  private Cart cart;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    cart = new Cart();
    user = createUser();
    cart.setUser(user);
    user.setCart(cart);
    when(userRepository.findByUsername("testUsername")).thenReturn(user);

    item = createItem();
    when(itemRepository.findById(1L)).thenReturn(java.util.Optional.of(item));

    request = createModifyCartRequest();
  }

  @Test
  public void addToCart_SUCCESS() {
    ResponseEntity<Cart> response = cartController.addToCart(request);
    assertNotNull(response);
    assertEquals(200, response.getStatusCodeValue());

    Cart cartInResponseBody = response.getBody();
    assertNotNull(cartInResponseBody);
    assertEquals(1, cartInResponseBody.getItems().size());
    assertEquals(item, cartInResponseBody.getItems().get(0));
    assertEquals(user, cartInResponseBody.getUser());
    assertEquals(new BigDecimal("15.78"), cartInResponseBody.getTotal());

    verify(userRepository, times(1)).findByUsername(anyString());
    verify(itemRepository, times(1)).findById(anyLong());
    verify(cartRepository, times(1)).save(isA(Cart.class));
  }

  @Test
  public void addToCart_FAIL_USER_NOT_FOUND() {
    request.setUsername("Different username");

    ResponseEntity<Cart> response = cartController.addToCart(request);
    assertNotNull(response);
    assertNull(response.getBody());
    assertEquals(404, response.getStatusCodeValue());

    verify(userRepository, times(1)).findByUsername(anyString());
    verify(itemRepository, times(0)).findById(anyLong());
    verify(cartRepository, times(0)).save(isA(Cart.class));
  }

  @Test
  public void addToCart_FAIL_ITEM_NOT_FOUND() {
    request.setItemId(2L);

    ResponseEntity<Cart> response = cartController.addToCart(request);
    assertNotNull(response);
    assertNull(response.getBody());
    assertEquals(404, response.getStatusCodeValue());

    verify(userRepository, times(1)).findByUsername(anyString());
    verify(itemRepository, times(1)).findById(anyLong());
    verify(cartRepository, times(0)).save(isA(Cart.class));
  }

  @Test
  public void removeFromCart_SUCCESS() {
    cart.addItem(item);

    ResponseEntity<Cart> response = cartController.removeFromCart(request);
    assertNotNull(response);
    assertEquals(200, response.getStatusCodeValue());

    Cart cartInResponseBody = response.getBody();
    assertNotNull(cartInResponseBody);
    assertEquals(0, cartInResponseBody.getItems().size());
    assertEquals(user, cartInResponseBody.getUser());
    assertEquals(new BigDecimal("0.00"), cartInResponseBody.getTotal());

    verify(userRepository, times(1)).findByUsername(anyString());
    verify(itemRepository, times(1)).findById(anyLong());
    verify(cartRepository, times(1)).save(isA(Cart.class));
  }

  @Test
  public void removeFromCart_USER_NOT_FOUND() {
    cart.addItem(item);

    request.setUsername("Different username");

    ResponseEntity<Cart> response = cartController.removeFromCart(request);
    assertNotNull(response);
    assertNull(response.getBody());
    assertEquals(404, response.getStatusCodeValue());

    verify(userRepository, times(1)).findByUsername(anyString());
    verify(itemRepository, times(0)).findById(anyLong());
    verify(cartRepository, times(0)).save(isA(Cart.class));
  }

  @Test
  public void removeFromCart_ITEM_NOT_FOUND() {
    cart.addItem(item);

    request.setItemId(2L);

    ResponseEntity<Cart> response = cartController.removeFromCart(request);
    assertNotNull(response);
    assertNull(response.getBody());
    assertEquals(404, response.getStatusCodeValue());

    verify(userRepository, times(1)).findByUsername(anyString());
    verify(itemRepository, times(1)).findById(anyLong());
    verify(cartRepository, times(0)).save(isA(Cart.class));
  }

  private User createUser() {
    User user = new User();
    user.setId(1L);
    user.setUsername("testUsername");
    user.setPassword("thisIsHashed");
    return user;
  }

  private Item createItem() {
    Item item = new Item();
    item.setId(1L);
    item.setName("Test item name");
    item.setPrice(new BigDecimal("15.78"));
    item.setDescription("Test item description");
    return item;
  }

  private ModifyCartRequest createModifyCartRequest() {
    ModifyCartRequest request = new ModifyCartRequest();
    request.setUsername("testUsername");
    request.setItemId(1L);
    request.setQuantity(1);
    return request;
  }

}
