package br.com.serasa.serasa.truck;

import br.com.serasa.serasa.common.PlateNormalizer;
import br.com.serasa.serasa.common.exception.ConflictException;
import br.com.serasa.serasa.common.exception.NotFoundException;
import br.com.serasa.serasa.truck.dto.TruckRequest;
import br.com.serasa.serasa.truck.dto.TruckResponse;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TruckService {

    private final TruckRepository repository;

    public TruckService(TruckRepository repository) {
        this.repository = repository;
    }

    public TruckResponse create(TruckRequest request) {
        String plate = PlateNormalizer.normalize(request.plate());
        if (repository.existsByPlate(plate)) {
            throw new ConflictException("Truck with plate '" + plate + "' already exists");
        }
        Truck truck = Truck.builder()
                .plate(plate)
                .tareWeightKg(request.tareWeightKg())
                .cargoCapacityTons(request.cargoCapacityTons())
                .build();
        return toResponse(repository.save(truck));
    }

    @Transactional(readOnly = true)
    public List<TruckResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TruckResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    public TruckResponse update(Long id, TruckRequest request) {
        Truck truck = getOrThrow(id);
        String plate = PlateNormalizer.normalize(request.plate());
        if (!truck.getPlate().equals(plate) && repository.existsByPlate(plate)) {
            throw new ConflictException("Truck with plate '" + plate + "' already exists");
        }
        truck.setPlate(plate);
        truck.setTareWeightKg(request.tareWeightKg());
        truck.setCargoCapacityTons(request.cargoCapacityTons());
        return toResponse(truck);
    }

    public void delete(Long id) {
        repository.delete(getOrThrow(id));
    }

    private Truck getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Truck " + id + " not found"));
    }

    private TruckResponse toResponse(Truck truck) {
        return new TruckResponse(truck.getId(), truck.getPlate(), truck.getTareWeightKg(), truck.getCargoCapacityTons());
    }
}
