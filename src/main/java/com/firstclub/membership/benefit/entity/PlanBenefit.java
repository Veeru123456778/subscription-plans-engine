package com.firstclub.membership.benefit.entity;

import com.firstclub.membership.plan.entity.Plan;
import com.firstclub.membership.tier.entity.Tier;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "plan_benefit")
@Getter
@Setter
@NoArgsConstructor
public class PlanBenefit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tier_id")
    private Tier tier;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(precision = 19, scale = 2)
    private BigDecimal value;

    @Column(name = "discount_type", length = 20)
    private String discountType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String eligibility;

    @Column(name = "monthly_limit")
    private Integer monthlyLimit;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public PlanBenefit(
            Plan plan,
            Tier tier,
            String type,
            BigDecimal value,
            String discountType,
            String eligibility,
            Integer monthlyLimit
    ) {
        this.plan = plan;
        this.tier = tier;
        this.type = type;
        this.value = value;
        this.discountType = discountType;
        this.eligibility = eligibility;
        this.monthlyLimit = monthlyLimit;
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
}