package br.com.serasa.serasa.truck;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TruckRepository extends JpaRepository<Truck, Long> {

    Optional<Truck> findByPlate(String plate);

    boolean existsByPlate(String plate);
}
