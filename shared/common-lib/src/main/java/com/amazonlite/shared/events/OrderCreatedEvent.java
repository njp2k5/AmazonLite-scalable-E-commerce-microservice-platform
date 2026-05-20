import java.math.BigDecimal;
import java.time.Instant;

public class OrderCreatedEvent {

    private String orderId;
    private String userId;
    private BigDecimal totalPrice;
    private Instant createdAt;

    public OrderCreatedEvent() {
    }

    public OrderCreatedEvent(String orderId, String userId, BigDecimal totalPrice, Instant createdAt) {
        this.orderId = orderId;
        this.userId = userId;
        this.totalPrice = totalPrice;
        this.createdAt = createdAt;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
