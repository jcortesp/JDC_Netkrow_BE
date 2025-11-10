package com.netkrow.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales", indexes = {
        @Index(name = "idx_sales_date", columnList = "sale_date"),
        @Index(name = "idx_sales_product", columnList = "product_id"),
        @Index(name = "idx_sales_channel", columnList = "channel"),
        @Index(name = "idx_sales_payment", columnList = "payment_method")
})
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sale_id")
    private Long saleId;

    @Column(name = "sale_date", nullable = false)
    private LocalDate saleDate;

    @Column(name = "remision_venta", length = 100)
    private String remisionVenta;

    @Column(name = "transaction_type", nullable = false, length = 60)
    private String transactionType;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "channel", nullable = false, length = 60)
    private String channel;

    @Column(name = "unit_qty", nullable = false)
    private Integer unitQty = 1;

    @Column(name = "sale_value", nullable = false, precision = 14, scale = 2)
    private BigDecimal saleValue;

    @Column(name = "payment_method", nullable = false, length = 60)
    private String paymentMethod;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters y setters
    public Long getSaleId() { return saleId; }
    public void setSaleId(Long saleId) { this.saleId = saleId; }

    public LocalDate getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDate saleDate) { this.saleDate = saleDate; }

    public String getRemisionVenta() { return remisionVenta; }
    public void setRemisionVenta(String remisionVenta) { this.remisionVenta = remisionVenta; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public Integer getUnitQty() { return unitQty; }
    public void setUnitQty(Integer unitQty) { this.unitQty = unitQty; }

    public BigDecimal getSaleValue() { return saleValue; }
    public void setSaleValue(BigDecimal saleValue) { this.saleValue = saleValue; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
