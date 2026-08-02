package br.com.serasa.serasa.branch;

import br.com.serasa.serasa.branch.dto.BranchRequest;
import br.com.serasa.serasa.branch.dto.BranchResponse;
import br.com.serasa.serasa.common.exception.ConflictException;
import br.com.serasa.serasa.common.exception.NotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BranchService {

    private final BranchRepository repository;

    public BranchService(BranchRepository repository) {
        this.repository = repository;
    }

    public BranchResponse create(BranchRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException("Branch with code '" + request.code() + "' already exists");
        }
        Branch branch = Branch.builder()
                .code(request.code())
                .name(request.name())
                .city(request.city())
                .state(request.state())
                .build();
        return toResponse(repository.save(branch));
    }

    @Transactional(readOnly = true)
    public List<BranchResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BranchResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    public BranchResponse update(Long id, BranchRequest request) {
        Branch branch = getOrThrow(id);
        if (!branch.getCode().equals(request.code()) && repository.existsByCode(request.code())) {
            throw new ConflictException("Branch with code '" + request.code() + "' already exists");
        }
        branch.setCode(request.code());
        branch.setName(request.name());
        branch.setCity(request.city());
        branch.setState(request.state());
        return toResponse(branch);
    }

    public void delete(Long id) {
        repository.delete(getOrThrow(id));
    }

    private Branch getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Branch " + id + " not found"));
    }

    private BranchResponse toResponse(Branch branch) {
        return new BranchResponse(branch.getId(), branch.getCode(), branch.getName(), branch.getCity(), branch.getState());
    }
}
