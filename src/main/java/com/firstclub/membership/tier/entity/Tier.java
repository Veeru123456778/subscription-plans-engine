package com.firstclub.membership.tier.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tier")
public class Tier {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private Integer rank;

    @Column(name = "min_order_count")
    private Integer minOrderCount;

    @Column(name = "min_order_value_monthly", precision = 10, scale = 2)
    private BigDecimal minOrderValueMonthly;

    // @JdbcTypeCode(SqlTypes.ARRAY) maps this straight to Postgres's native
    // TEXT[] column type. Without it, Hibernate would try to serialize a
    // String[] as one big VARCHAR blob instead of a real array — this
    // annotation is what makes it round-trip correctly.
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "cohort_tags")
    private String[] cohortTags;

    // @Enumerated(EnumType.STRING) stores the enum's name ("ANY"/"ALL") as
    // text in the DB, not its ordinal position (0/1). Storing the ordinal is
    // a classic footgun: reordering the enum's constants later would silently
    // corrupt existing data, since the stored numbers would now point at
    // different values.
    @Enumerated(EnumType.STRING)
    @Column(name = "criteria_match_mode", nullable = false, length = 10)
    private MatchMode criteriaMatchMode;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Tier() {
    }

    public Tier(String name, Integer rank, MatchMode criteriaMatchMode) {
        this.name = name;
        this.rank = rank;
        this.criteriaMatchMode = criteriaMatchMode;
        this.active = true;
    }

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

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Integer getMinOrderCount() {
        return minOrderCount;
    }

    public void setMinOrderCount(Integer minOrderCount) {
        this.minOrderCount = minOrderCount;
    }

    public BigDecimal getMinOrderValueMonthly() {
        return minOrderValueMonthly;
    }

    public void setMinOrderValueMonthly(BigDecimal minOrderValueMonthly) {
        this.minOrderValueMonthly = minOrderValueMonthly;
    }

    public String[] getCohortTags() {
        return cohortTags;
    }

    public void setCohortTags(String[] cohortTags) {
        this.cohortTags = cohortTags;
    }

    public MatchMode getCriteriaMatchMode() {
        return criteriaMatchMode;
    }

    public void setCriteriaMatchMode(MatchMode criteriaMatchMode) {
        this.criteriaMatchMode = criteriaMatchMode;
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

    public enum MatchMode {
        ANY,
        ALL
    }
}
