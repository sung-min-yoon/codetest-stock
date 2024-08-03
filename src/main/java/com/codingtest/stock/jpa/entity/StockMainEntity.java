package com.codingtest.stock.jpa.entity;

import com.codingtest.stock.enums.StockStatusCodeEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.proxy.HibernateProxy;

import java.util.List;
import java.util.Objects;

import static jakarta.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "STOCK_MAIN")
@Getter
@Builder
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PACKAGE)
public class StockMainEntity {
    @Id
    @Column(name = "PRODUCT_ID")
    private String productId;

    @Column(name = "VERSION")
    private int version;

    @Column(name = "QUANTITY")
    private int quantity;

    @Column(name = "SAFETY_QUANTITY")
    private int safetyQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "STOCK_STATUS_CODE")
    private StockStatusCodeEnum stockStatusCode;

    @OneToMany(mappedBy = "stockMainEntity", fetch = LAZY)
    private List<StockHistoryEntity> stockHistoryEntitiyList;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        StockMainEntity that = (StockMainEntity) o;
        return getProductId() != null && Objects.equals(getProductId(), that.getProductId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}