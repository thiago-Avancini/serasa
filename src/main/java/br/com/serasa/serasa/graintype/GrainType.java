package br.com.serasa.serasa.graintype;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "grain_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrainType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String name;

    @Column(name = "purchase_price_per_ton", nullable = false, precision = 19, scale = 2)
    private BigDecimal purchasePricePerTon;

    @Column(name = "available_quantity_tons", nullable = false, precision = 19, scale = 3)
    private BigDecimal availableQuantityTons;

    @Version
    private Long version;
}
