package com.codingtest.stock.jpa.entity;

import com.codingtest.stock.enums.StockOperationCodeEnum;
import com.codingtest.stock.enums.StockStatusCodeEnum;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.util.Objects;

import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "STOCK_HISTORY", indexes = {@Index(name = "IDX_STOCK_HISTORY_PRODUCT_ID", columnList = "PRODUCT_ID"), @Index(name = "IDX_STOCK_HISTORY_ORDER_ID", columnList = "ORDER_ID")})
@Getter
@Builder
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PACKAGE)
public class StockHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "PRODUCT_ID", referencedColumnName = "PRODUCT_ID", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    private StockMainEntity stockMainEntity;

    @Column(name = "ORDER_ID")
    @Builder.Default
    private String orderId = StockOperationCodeEnum.INITIALISE.name();

    @Column(name = "VERSION")
    private int version;

    @Column(name = "QUANTITY")
    private int quantity;

    @Column(name = "SAFETY_QUANTITY")
    private int safetyQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "STOCK_STATUS_CODE")
    private StockStatusCodeEnum stockStatusCode;

    @Enumerated(EnumType.STRING)
    private StockOperationCodeEnum stockOperationCode;

    @Column(name = "TIMESTAMP")
    private LocalDateTime timestamp;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        StockHistoryEntity that = (StockHistoryEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
