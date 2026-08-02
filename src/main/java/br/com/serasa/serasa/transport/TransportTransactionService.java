package br.com.serasa.serasa.transport;

import br.com.serasa.serasa.branch.Branch;
import br.com.serasa.serasa.branch.BranchRepository;
import br.com.serasa.serasa.common.exception.ConflictException;
import br.com.serasa.serasa.common.exception.NotFoundException;
import br.com.serasa.serasa.graintype.GrainType;
import br.com.serasa.serasa.graintype.GrainTypeRepository;
import br.com.serasa.serasa.scale.Scale;
import br.com.serasa.serasa.scale.ScaleRepository;
import br.com.serasa.serasa.transport.dto.StartTransportTransactionRequest;
import br.com.serasa.serasa.transport.dto.TransportTransactionResponse;
import br.com.serasa.serasa.truck.Truck;
import br.com.serasa.serasa.truck.TruckRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TransportTransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransportTransactionService.class);
    private static final List<TransactionStatus> OPEN_STATUSES = List.of(TransactionStatus.STARTED, TransactionStatus.WEIGHING);
    private static final BigDecimal KG_PER_TON = BigDecimal.valueOf(1000);

    private final TransportTransactionRepository repository;
    private final TruckRepository truckRepository;
    private final GrainTypeRepository grainTypeRepository;
    private final BranchRepository branchRepository;
    private final ScaleRepository scaleRepository;

    public TransportTransactionService(
            TransportTransactionRepository repository,
            TruckRepository truckRepository,
            GrainTypeRepository grainTypeRepository,
            BranchRepository branchRepository,
            ScaleRepository scaleRepository
    ) {
        this.repository = repository;
        this.truckRepository = truckRepository;
        this.grainTypeRepository = grainTypeRepository;
        this.branchRepository = branchRepository;
        this.scaleRepository = scaleRepository;
    }

    public TransportTransactionResponse start(StartTransportTransactionRequest request) {
        Truck truck = truckRepository.findById(request.truckId())
                .orElseThrow(() -> new NotFoundException("Truck " + request.truckId() + " not found"));
        GrainType grainType = grainTypeRepository.findById(request.grainTypeId())
                .orElseThrow(() -> new NotFoundException("Grain type " + request.grainTypeId() + " not found"));
        Branch originBranch = branchRepository.findById(request.originBranchId())
                .orElseThrow(() -> new NotFoundException("Branch " + request.originBranchId() + " not found"));

        if (repository.existsByTruck_IdAndStatusIn(truck.getId(), OPEN_STATUSES)) {
            throw new ConflictException("Truck '" + truck.getPlate() + "' already has an open transport transaction");
        }

        TransportTransaction transaction = TransportTransaction.builder()
                .truck(truck)
                .grainType(grainType)
                .originBranch(originBranch)
                .status(TransactionStatus.STARTED)
                .startedAt(Instant.now())
                .build();

        return toResponse(repository.save(transaction));
    }

    public void markWeighingStarted(String plate, String scaleCode) {
        TransportTransaction transaction = repository.findFirstByTruck_PlateAndStatus(plate, TransactionStatus.STARTED)
                .orElse(null);
        if (transaction == null) {
            log.warn("No STARTED transport transaction found for plate '{}' on scale '{}'", plate, scaleCode);
            return;
        }
        Scale scale = scaleRepository.findById(scaleCode)
                .orElseThrow(() -> new NotFoundException("Scale '" + scaleCode + "' not found"));

        transaction.setScale(scale);
        transaction.setStatus(TransactionStatus.WEIGHING);
        transaction.setWeighingStartedAt(Instant.now());
    }

    public void completeWeighing(String plate, BigDecimal stabilizedGrossWeightKg) {
        TransportTransaction transaction = repository.findFirstByTruck_PlateAndStatus(plate, TransactionStatus.WEIGHING)
                .orElse(null);
        if (transaction == null) {
            log.warn("No WEIGHING transport transaction found for plate '{}'", plate);
            return;
        }

        BigDecimal grossWeightKg = stabilizedGrossWeightKg.setScale(2, RoundingMode.HALF_UP);
        BigDecimal tareWeightKg = transaction.getTruck().getTareWeightKg();
        BigDecimal netWeightKg = grossWeightKg.subtract(tareWeightKg).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netWeightTons = netWeightKg.divide(KG_PER_TON, 6, RoundingMode.HALF_UP);
        GrainType grainType = transaction.getGrainType();
        BigDecimal purchasePricePerTon = grainType.getPurchasePricePerTon();
        BigDecimal cargoCost = netWeightTons
                .multiply(purchasePricePerTon)
                .setScale(2, RoundingMode.HALF_UP);

        transaction.setGrossWeightKg(grossWeightKg);
        transaction.setTareWeightKg(tareWeightKg);
        transaction.setNetWeightKg(netWeightKg);
        transaction.setPurchasePriceSnapshot(purchasePricePerTon);
        transaction.setCargoCost(cargoCost);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setWeighingCompletedAt(Instant.now());

        grainTypeRepository.incrementAvailableQuantity(grainType.getId(), netWeightTons);
    }

    @Transactional(readOnly = true)
    public List<TransportTransactionResponse> findAll(TransactionStatus status) {
        List<TransportTransaction> transactions = status == null
                ? repository.findAll()
                : repository.findAllByStatus(status);
        return transactions.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TransportTransactionResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    private TransportTransaction getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transport transaction " + id + " not found"));
    }

    private TransportTransactionResponse toResponse(TransportTransaction transaction) {
        return new TransportTransactionResponse(
                transaction.getId(),
                transaction.getTruck().getPlate(),
                transaction.getGrainType().getName(),
                transaction.getOriginBranch().getName(),
                transaction.getScale() != null ? transaction.getScale().getCode() : null,
                transaction.getStatus(),
                transaction.getStartedAt(),
                transaction.getWeighingStartedAt(),
                transaction.getWeighingCompletedAt(),
                transaction.getGrossWeightKg(),
                transaction.getTareWeightKg(),
                transaction.getNetWeightKg(),
                transaction.getPurchasePriceSnapshot(),
                transaction.getCargoCost()
        );
    }
}
