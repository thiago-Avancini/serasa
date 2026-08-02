package br.com.serasa.serasa.transport;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransportTransactionRepository extends JpaRepository<TransportTransaction, Long> {

    List<TransportTransaction> findAllByStatus(TransactionStatus status);

    Optional<TransportTransaction> findFirstByTruck_PlateAndStatus(String plate, TransactionStatus status);

    boolean existsByTruck_IdAndStatusIn(Long truckId, List<TransactionStatus> statuses);

    interface GrainTypeAggregate {
        Long getGrainTypeId();

        Long getCompletedTransactions();

        BigDecimal getTotalNetWeightKg();

        BigDecimal getTotalCargoCost();
    }

    interface BranchAggregate {
        Long getBranchId();

        Long getCompletedTransactions();

        BigDecimal getTotalNetWeightKg();

        BigDecimal getTotalCargoCost();
    }

    interface ScaleAggregate {
        String getScaleCode();

        Long getCompletedWeighings();

        Double getAverageStabilizationSeconds();
    }

    interface StatusCount {
        TransactionStatus getStatus();

        Long getCount();
    }

    @Query("""
            SELECT t.grainType.id AS grainTypeId, COUNT(t) AS completedTransactions,
                   COALESCE(SUM(t.netWeightKg), 0) AS totalNetWeightKg,
                   COALESCE(SUM(t.cargoCost), 0) AS totalCargoCost
            FROM TransportTransaction t
            WHERE t.status = :status
            GROUP BY t.grainType.id
            """)
    List<GrainTypeAggregate> aggregateByGrainType(@Param("status") TransactionStatus status);

    @Query("""
            SELECT t.originBranch.id AS branchId, COUNT(t) AS completedTransactions,
                   COALESCE(SUM(t.netWeightKg), 0) AS totalNetWeightKg,
                   COALESCE(SUM(t.cargoCost), 0) AS totalCargoCost
            FROM TransportTransaction t
            WHERE t.status = :status
            GROUP BY t.originBranch.id
            """)
    List<BranchAggregate> aggregateByBranch(@Param("status") TransactionStatus status);

    @Query(value = """
            SELECT scale_code AS scaleCode, COUNT(*) AS completedWeighings,
                   AVG(EXTRACT(EPOCH FROM (weighing_completed_at - weighing_started_at))) AS averageStabilizationSeconds
            FROM transport_transaction
            WHERE status = 'COMPLETED' AND scale_code IS NOT NULL
            GROUP BY scale_code
            """, nativeQuery = true)
    List<ScaleAggregate> aggregateByScale();

    @Query("""
            SELECT t.status AS status, COUNT(t) AS count
            FROM TransportTransaction t
            GROUP BY t.status
            """)
    List<StatusCount> countByStatus();
}
