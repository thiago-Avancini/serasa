package br.com.serasa.serasa.graintype;

import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GrainTypeRepository extends JpaRepository<GrainType, Long> {

    Optional<GrainType> findByName(String name);

    boolean existsByName(String name);

    /**
     * Atomic increment instead of read-modify-write: concurrent weighings completing for the
     * same grain type would otherwise race on {@code availableQuantityTons} and silently lose
     * one of the updates. The database row lock during this UPDATE serializes them correctly.
     */
    @Modifying
    @Query("""
            UPDATE GrainType g
            SET g.availableQuantityTons = g.availableQuantityTons + :deltaTons,
                g.version = g.version + 1
            WHERE g.id = :id
            """)
    void incrementAvailableQuantity(@Param("id") Long id, @Param("deltaTons") BigDecimal deltaTons);
}