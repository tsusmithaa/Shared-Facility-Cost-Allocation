
package com.sharedfacility.invoice;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_id")
    private String invoiceId;

    private String provider;

    @Column(name = "billing_period")
    private String billingPeriod;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "common_area_percent")
    private BigDecimal commonAreaPercent;

    private String status;

    public Invoice() {
    }

    public Long getId() {
        return id;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public String getProvider() {
        return provider;
    }

    public String getBillingPeriod() {
        return billingPeriod;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BigDecimal getCommonAreaPercent() {
        return commonAreaPercent;
    }

    public String getStatus() {
        return status;
    }
}
