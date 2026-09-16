package com.amazonlite.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    classes = GatewayApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "jwt.secret=0123456789abcdef0123456789abcdef"
)
class GatewayApplicationTest {

    @Autowired
    private RouteLocator routeLocator;

    @Test
    void shouldExposePublicAndProtectedRoutes() {
        List<Route> routes = routeLocator.getRoutes()
                .collectList()
                .block(Duration.ofSeconds(5));

        assertThat(routes).isNotNull();

        Map<String, Route> routeMap = routes.stream().collect(Collectors.toMap(Route::getId, Function.identity()));
        assertThat(routeMap.keySet()).contains("auth", "products-public", "cart-protected", "buy-protected");
        assertThat(routeMap.get("auth").getFilters()).isNotEmpty();
        assertThat(routeMap.get("products-public").getFilters()).isNotEmpty();
        assertThat(routeMap.get("cart-protected").getFilters()).isNotEmpty();
        assertThat(routeMap.get("buy-protected").getFilters()).isNotEmpty();
    }
}
