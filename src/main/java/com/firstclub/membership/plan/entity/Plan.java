package com.firstclub.membership.plan.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

// @Entity tells Hibernate this class maps to a database table.
// @Table names that table explicitly; without it, Hibernate would default to
// the class name ("Plan" -> "plan"), which happens to match here anyway.
@Entity
@Table(name = "plan")
public class Plan {

    @Id
    // GenerationType.UUID (Hibernate 6+) generates the UUID in the JVM before
    // the INSERT is sent, so plan.getId() is already populated right after
    // "new Plan(...)" — we don't have to wait for a DB round trip to get it.
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // JPA requires a no-arg constructor so Hibernate can instantiate entities
    // via reflection when loading rows from the database. It's never meant to
    // be called directly by our own code, hence "protected" instead of "public".
    protected Plan() {
    }

    public Plan(String name, Integer durationDays, BigDecimal price, String currency) {
        this.name = name;
        this.durationDays = durationDays;
        this.price = price;
        this.currency = currency;
        this.active = true;
    }

    // @PrePersist / @PreUpdate are JPA lifecycle callbacks: Hibernate calls
    // these methods automatically right before an INSERT / UPDATE, so we never
    // have to remember to set timestamps manually in service code.
    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    // Deliberately not overriding equals()/hashCode() here. Basing them on
    // mutable fields (or even the id, before it's assigned) is a well-known
    // JPA footgun — an entity can behave inconsistently in a HashSet if its
    // hashCode changes between being added and being looked up. Falling back
    // to Object's identity-based equals/hashCode is the safest default for
    // an entity we're not putting into hash-based collections.
}
