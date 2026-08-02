package br.com.serasa.serasa.scale;

import br.com.serasa.serasa.branch.Branch;
import br.com.serasa.serasa.branch.BranchRepository;
import br.com.serasa.serasa.common.exception.ConflictException;
import br.com.serasa.serasa.common.exception.NotFoundException;
import br.com.serasa.serasa.scale.dto.ScaleRequest;
import br.com.serasa.serasa.scale.dto.ScaleResponse;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ScaleService {

    private final ScaleRepository repository;
    private final BranchRepository branchRepository;

    public ScaleService(ScaleRepository repository, BranchRepository branchRepository) {
        this.repository = repository;
        this.branchRepository = branchRepository;
    }

    public ScaleResponse create(ScaleRequest request) {
        if (repository.existsById(request.code())) {
            throw new ConflictException("Scale with code '" + request.code() + "' already exists");
        }
        Branch branch = getBranchOrThrow(request.branchId());
        Scale scale = Scale.builder()
                .code(request.code())
                .branch(branch)
                .location(request.location())
                .active(request.active() == null || request.active())
                .build();
        return toResponse(repository.save(scale));
    }

    @Transactional(readOnly = true)
    public List<ScaleResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ScaleResponse findByCode(String code) {
        return toResponse(getOrThrow(code));
    }

    public ScaleResponse update(String code, ScaleRequest request) {
        Scale scale = getOrThrow(code);
        Branch branch = getBranchOrThrow(request.branchId());
        scale.setBranch(branch);
        scale.setLocation(request.location());
        scale.setActive(request.active() == null || request.active());
        return toResponse(scale);
    }

    public void delete(String code) {
        repository.delete(getOrThrow(code));
    }

    private Scale getOrThrow(String code) {
        return repository.findById(code)
                .orElseThrow(() -> new NotFoundException("Scale '" + code + "' not found"));
    }

    private Branch getBranchOrThrow(Long branchId) {
        return branchRepository.findById(branchId)
                .orElseThrow(() -> new NotFoundException("Branch " + branchId + " not found"));
    }

    private ScaleResponse toResponse(Scale scale) {
        return new ScaleResponse(
                scale.getCode(),
                scale.getBranch().getId(),
                scale.getBranch().getName(),
                scale.getLocation(),
                scale.isActive()
        );
    }
}
