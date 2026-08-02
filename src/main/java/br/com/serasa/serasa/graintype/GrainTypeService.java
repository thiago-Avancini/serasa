package br.com.serasa.serasa.graintype;

import br.com.serasa.serasa.common.exception.ConflictException;
import br.com.serasa.serasa.common.exception.NotFoundException;
import br.com.serasa.serasa.graintype.dto.GrainTypeRequest;
import br.com.serasa.serasa.graintype.dto.GrainTypeResponse;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GrainTypeService {

    private final GrainTypeRepository repository;
    private final GrainPricingService pricingService;

    public GrainTypeService(GrainTypeRepository repository, GrainPricingService pricingService) {
        this.repository = repository;
        this.pricingService = pricingService;
    }

    public GrainTypeResponse create(GrainTypeRequest request) {
        if (repository.existsByName(request.name())) {
            throw new ConflictException("Grain type '" + request.name() + "' already exists");
        }
        GrainType grainType = GrainType.builder()
                .name(request.name())
                .purchasePricePerTon(request.purchasePricePerTon())
                .availableQuantityTons(request.availableQuantityTons())
                .build();
        return toResponse(repository.save(grainType));
    }

    @Transactional(readOnly = true)
    public List<GrainTypeResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public GrainTypeResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    public GrainTypeResponse update(Long id, GrainTypeRequest request) {
        GrainType grainType = getOrThrow(id);
        if (!grainType.getName().equals(request.name()) && repository.existsByName(request.name())) {
            throw new ConflictException("Grain type '" + request.name() + "' already exists");
        }
        grainType.setName(request.name());
        grainType.setPurchasePricePerTon(request.purchasePricePerTon());
        grainType.setAvailableQuantityTons(request.availableQuantityTons());
        return toResponse(grainType);
    }

    public void delete(Long id) {
        repository.delete(getOrThrow(id));
    }

    private GrainType getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Grain type " + id + " not found"));
    }

    private GrainTypeResponse toResponse(GrainType grainType) {
        return new GrainTypeResponse(
                grainType.getId(),
                grainType.getName(),
                grainType.getPurchasePricePerTon(),
                grainType.getAvailableQuantityTons(),
                pricingService.marginPercentage(grainType),
                pricingService.salePricePerTon(grainType)
        );
    }
}
