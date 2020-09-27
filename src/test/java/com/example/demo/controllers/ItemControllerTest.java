package com.example.demo.controllers;

import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.repositories.ItemRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ItemControllerTest {

  @Mock
  private ItemRepository itemRepository;

  @InjectMocks
  private ItemController itemController;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void getItems() {
    Item item = createItem();
    List<Item> itemsInDB = new ArrayList<>();
    itemsInDB.add(item);

    when(itemRepository.findAll()).thenReturn(itemsInDB);

    ResponseEntity<List<Item>> response = itemController.getItems();
    assertNotNull(response);
    assertEquals(200, response.getStatusCodeValue());

    List<Item> itemsInResponseBody = response.getBody();
    assertNotNull(itemsInResponseBody);
    assertArrayEquals(itemsInDB.toArray(), itemsInResponseBody.toArray());

    verify(itemRepository, times(1)).findAll();
  }

  @Test
  public void getItemById_FOUND() {
    Item itemInDB = createItem();
    when(itemRepository.findById(1L)).thenReturn(Optional.of(itemInDB));

    ResponseEntity<Item> response = itemController.getItemById(1L);
    assertNotNull(response);
    assertEquals(200, response.getStatusCodeValue());

    Item itemInResponseBody = response.getBody();
    assertNotNull(itemInResponseBody);
    assertEquals((Long) 1L, itemInResponseBody.getId());
    assertEquals("Test item name", itemInResponseBody.getName());
    assertEquals("Test item description", itemInResponseBody.getDescription());
    assertEquals(new BigDecimal("15.78"), itemInResponseBody.getPrice());

    verify(itemRepository, times(1)).findById(anyLong());
  }

  @Test
  public void getItemById_NOT_FOUND() {
    Item itemInDB = createItem();
    when(itemRepository.findById(1L)).thenReturn(Optional.of(itemInDB));

    ResponseEntity<Item> response = itemController.getItemById(2L);
    assertNotNull(response);
    assertNull(response.getBody());
    assertEquals(404, response.getStatusCodeValue());

    verify(itemRepository, times(1)).findById(anyLong());
  }

  @Test
  public void getItemsByName_FOUND() {
    Item item = createItem();
    List<Item> itemsInDB = new ArrayList<>();
    itemsInDB.add(item);

    when(itemRepository.findByName("Test item name")).thenReturn(itemsInDB);

    ResponseEntity<List<Item>> response = itemController.getItemsByName("Test item name");
    assertNotNull(response);
    assertEquals(200, response.getStatusCodeValue());

    List<Item> itemsInResponseBody = response.getBody();
    assertNotNull(itemsInResponseBody);
    assertArrayEquals(itemsInDB.toArray(), itemsInResponseBody.toArray());

    verify(itemRepository, times(1)).findByName(anyString());
  }

  @Test
  public void getItemsByName_NOT_FOUND() {
    Item item = createItem();
    List<Item> itemsInDB = new ArrayList<>();
    itemsInDB.add(item);

    when(itemRepository.findByName("Test item name")).thenReturn(itemsInDB);

    ResponseEntity<List<Item>> response = itemController.getItemsByName("Test item different name");
    assertNotNull(response);
    assertNull(response.getBody());
    assertEquals(404, response.getStatusCodeValue());

    verify(itemRepository, times(1)).findByName(anyString());
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
