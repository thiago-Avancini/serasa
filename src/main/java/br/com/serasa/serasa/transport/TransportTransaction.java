package br.com.serasa.serasa.transport;

import br.com.serasa.serasa.branch.Branch;
import br.com.serasa.serasa.graintype.GrainType;
import br.com.serasa.serasa.scale.Scale;
import br.com.serasa.serasa.truck.Truck;
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
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "transport_transaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransportTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "truck_id", nullable = false)
    private Truck truck;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "grain_type_id", nullable = false)
    private GrainType grainType;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_branch_id", nullable = false)
    private Branch originBranch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scale_code")
    private Scale scale;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "weighing_started_at")
    private Instant weighingStartedAt;

    @Column(name = "weighing_completed_at")
    private Instant weighingCompletedAt;

    @Column(name = "gross_weight_kg", precision = 19, scale = 2)
    private BigDecimal grossWeightKg;

    @Column(name = "tare_weight_kg", precision = 19, scale = 2)
    private BigDecimal tareWeightKg;

    @Column(name = "net_weight_kg", precision = 19, scale = 2)
    private BigDecimal netWeightKg;

    @Column(name = "purchase_price_snapshot", precision = 19, scale = 2)
    private BigDecimal purchasePriceSnapshot;

    @Column(name = "cargo_cost", precision = 19, scale = 2)
    private BigDecimal cargoCost;
}
