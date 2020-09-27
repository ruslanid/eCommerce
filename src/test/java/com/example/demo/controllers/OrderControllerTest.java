package com.example.demo.controllers;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.persistence.repositories.OrderRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class OrderControllerTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private OrderRepository orderRepository;

  @InjectMocks
  private OrderController orderController;

  private User user;

  private Item item;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    user = createUser();

    item = createItem();
    List<Item> items = new ArrayList<>();
    items.add(item);

    Cart cart = new Cart();
    cart.setId(1L);
    cart.setItems(items);
    cart.setUser(user);
    cart.setTotal(new BigDecimal("15.78"));

    user.setCart(cart);
  }

  @Test
  public void submit_SUCCESS() {
    when(userRepository.findByUsername("testUsername")).thenReturn(user);

    ResponseEntity<UserOrder> response = orderController.submit("testUsername");
    assertNotNull(response);
    assertEquals(200, response.getStatusCodeValue());

    UserOrder orderInResponseBody = response.getBody();
    assertNotNull(orderInResponseBody);
    assertEquals(1, orderInResponseBody.getItems().size());
    assertEquals(item, orderInResponseBody.getItems().get(0));
    assertEquals(user, orderInResponseBody.getUser());
    assertEquals(new BigDecimal("15.78"), orderInResponseBody.getTotal());

    verify(userRepository, times(1)).findByUsername(anyString());
    verify(orderRepository, times(1)).save(isA(UserOrder.class));
  }

  @Test
  public void submit_FAIL_USER_NOT_FOUND() {
    when(userRepository.findByUsername("testUsername")).thenReturn(user);

    ResponseEntity<UserOrder> response = orderController.submit("differentName");
    assertNotNull(response);
    assertNull(response.getBody());
    assertEquals(404, response.getStatusCodeValue());

    verify(userRepository, times(1)).findByUsername(anyString());
    verify(orderRepository, times(0)).save(isA(UserOrder.class));
  }

  @Test
  public void getOrdersForUser_SUCCESS() {
    when(userRepository.findByUsername("testUsername")).thenReturn(user);

    UserOrder order = UserOrder.createFromCart(user.getCart());
    List<UserOrder> orders = new ArrayList<>();
    orders.add(order);

    when(orderRepository.findByUser(user)).thenReturn(orders);

    ResponseEntity<List<UserOrder>> response = orderController.getOrdersForUser("testUsername");
    assertNotNull(response);
    assertEquals(200, response.getStatusCodeValue());

    List<UserOrder> ordersInResponseBody = response.getBody();
    assertNotNull(ordersInResponseBody);
    assertEquals(1, ordersInResponseBody.size());
    assertEquals(item, ordersInResponseBody.get(0).getItems().get(0));
    assertEquals(user, ordersInResponseBody.get(0).getUser());
    assertEquals(new BigDecimal("15.78"), ordersInResponseBody.get(0).getTotal());

    verify(userRepository, times(1)).findByUsername(anyString());
    verify(orderRepository, times(1)).findByUser(isA(User.class));
  }

  @Test
  public void getOrdersForUser_USER_NOT_FOUND() {
    when(userRepository.findByUsername("testUsername")).thenReturn(user);

    UserOrder order = UserOrder.createFromCart(user.getCart());
    List<UserOrder> orders = new ArrayList<>();
    orders.add(order);

    when(orderRepository.findByUser(user)).thenReturn(orders);

    ResponseEntity<List<UserOrder>> response = orderController.getOrdersForUser("differentName");
    assertNotNull(response);
    assertNull(response.getBody());
    assertEquals(404, response.getStatusCodeValue());

    verify(userRepository, times(1)).findByUsername(anyString());
    verify(orderRepository, times(0)).findByUser(isA(User.class));
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

}
