package com.firstclub.membership.membership.entity;

import com.firstclub.membership.plan.entity.Plan;
import com.firstclub.membership.plan.entity.PlanPrice;
import com.firstclub.membership.tier.entity.Tier;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "membership")
@Getter
@Setter
@NoArgsConstructor
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_price_id", nullable = false)
    private PlanPrice planPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_tier_id")
    private Tier currentTier;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier_source", nullable = false, length = 30)
    private TierSource tierSource;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MembershipStatus status;

    @Column(name = "start_date", nullable = false)
    private Instant startDate;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Membership(
            UUID userId,
            Plan plan,
            PlanPrice planPrice,
            Tier currentTier,
            TierSource tierSource,
            Instant startDate,
            Instant expiryDate
    ) {
        this.userId = userId;
        this.plan = plan;
        this.planPrice = planPrice;
        this.currentTier = currentTier;
        this.tierSource = tierSource;
        this.status = MembershipStatus.ACTIVE;
        this.startDate = startDate;
        this.expiryDate = expiryDate;
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