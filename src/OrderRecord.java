
public class OrderRecord {

    private final String orderId;
    private final int actualShippingDays;
    private final int scheduledShippingDays;
    private final double orderProfitPerOrder;
    private final String deliveryStatus;
    private final String shippingMode;
    private final String market;
    private final String orderRegion;

    public OrderRecord(
            String orderId,
            int actualShippingDays,
            int scheduledShippingDays,
            double orderProfitPerOrder,
            String deliveryStatus,
            String shippingMode,
            String market,
            String orderRegion) {

        this.orderId = orderId;
        this.actualShippingDays = actualShippingDays;
        this.scheduledShippingDays = scheduledShippingDays;
        this.orderProfitPerOrder = orderProfitPerOrder;
        this.deliveryStatus = deliveryStatus;
        this.shippingMode = shippingMode;
        this.market = market;
        this.orderRegion = orderRegion;
    }

    public String getOrderId() {
        return orderId;
    }

    public int getActualShippingDays() {
        return actualShippingDays;
    }

    public int getScheduledShippingDays() {
        return scheduledShippingDays;
    }

    public double getOrderProfitPerOrder() {
        return orderProfitPerOrder;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public String getShippingMode() {
        return shippingMode;
    }

    public String getMarket() {
        return market;
    }

    public String getOrderRegion() {
        return orderRegion;
    }

    public int getShippingDifference() {
        return actualShippingDays - scheduledShippingDays;
    }
}

