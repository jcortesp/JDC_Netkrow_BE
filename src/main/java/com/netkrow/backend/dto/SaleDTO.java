package com.netkrow.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SaleDTO {
    private Long saleId;
    private LocalDate saleDate;       // YYYY-MM-DD
    private String remisionVenta;
    private String transactionType;
    private Long productId;           // dropdown products
    private String channel;
    private Integer unitQty;
    private BigDecimal saleValue;
    private String paymentMethod;

    // Getters y setters
    public Long getSaleId() { return saleId; }
    public void setSaleId(Long saleId) { this.saleId = saleId; }

    public LocalDate getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDate saleDate) { this.saleDate = saleDate; }

    public String getRemisionVenta() { return remisionVenta; }
    public void setRemisionVenta(String remisionVenta) { this.remisionVenta = remisionVenta; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public Integer getUnitQty() { return unitQty; }
    public void setUnitQty(Integer unitQty) { this.unitQty = unitQty; }

    public BigDecimal getSaleValue() { return saleValue; }
    public void setSaleValue(BigDecimal saleValue) { this.saleValue = saleValue; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}
