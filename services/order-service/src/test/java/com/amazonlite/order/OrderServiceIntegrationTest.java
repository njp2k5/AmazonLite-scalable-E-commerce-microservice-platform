package com.amazonlite.order;

import com.amazonlite.order.model.Order;
import com.amazonlite.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class OrderServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private OrderRepository orderRepository;

    // 1. successful order creation
    @Test
    void testCreateOrderSuccess() throws Exception {
        String requestBody = "{" +
                "\"productId\": 1," +
                "\"quantity\": 1" +
                "}";
        mockMvc.perform(post("/orders")
                .header("X-User-Id", 100)
                .header("X-User-Role", "CUSTOMER")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").exists())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    // 2. insufficient stock returns 409
    @Test
    void testCreateOrderInsufficientStock() throws Exception {
        String requestBody = "{" +
                "\"productId\": 1," +
                "\"quantity\": 9999" +
                "}";
        mockMvc.perform(post("/orders")
                .header("X-User-Id", 100)
                .header("X-User-Role", "CUSTOMER")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isConflict());
    }

    // 3. unauthorized access returns 403
    @Test
    void testUnauthorizedAccess() throws Exception {
        String requestBody = "{" +
                "\"productId\": 1," +
                "\"quantity\": 1" +
                "}";
        mockMvc.perform(post("/orders")
                .header("X-User-Id", 100)
                .header("X-User-Role", "GUEST")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isForbidden());
    }

    // 4. cancel shipped order returns 409
    @Test
    void testCancelShippedOrderReturns409() throws Exception {
        // Setup: create and mark an order as SHIPPED
        Order order = new Order();
        order.setUserId(100L);
        order.setStatus(com.amazonlite.order.model.OrderStatus.SHIPPED);
        order.setTotalPrice(java.math.BigDecimal.TEN);
        order = orderRepository.save(order);

        mockMvc.perform(patch("/orders/" + order.getId() + "/cancel")
                .header("X-User-Id", 100)
                .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isConflict());
    }
}
