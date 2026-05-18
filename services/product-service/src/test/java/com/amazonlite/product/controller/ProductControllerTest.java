package com.amazonlite.product.controller;

import com.amazonlite.product.model.Product;
import com.amazonlite.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
    }

    @Test
    void shouldExposeProductsWithoutAuthentication() throws Exception {
        when(productService.getProducts(anyInt(), anyInt())).thenReturn(Map.of(
                "content", List.of(Map.of("id", 1, "name", "Phone")),
                "page", 0,
                "size", 10,
                "totalElements", 1
        ));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Phone"));
    }

    @Test
    void shouldReturnProductByIdWithoutAuthentication() throws Exception {
        Product product = new Product();
        product.setId(5L);
        product.setName("Laptop");
        when(productService.getById(anyLong())).thenReturn(Optional.of(product));

        mockMvc.perform(get("/products/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laptop"));
    }
}
