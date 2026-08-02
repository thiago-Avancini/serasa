package br.com.serasa.serasa.report;

import br.com.serasa.serasa.branch.Branch;
import br.com.serasa.serasa.branch.BranchRepository;
import br.com.serasa.serasa.graintype.GrainPricingService;
import br.com.serasa.serasa.graintype.GrainType;
import br.com.serasa.serasa.graintype.GrainTypeRepository;
import br.com.serasa.serasa.report.dto.BranchReportItem;
import br.com.serasa.serasa.report.dto.GrainTypeReportItem;
import br.com.serasa.serasa.report.dto.OverviewReport;
import br.com.serasa.serasa.report.dto.ScaleReportItem;
import br.com.serasa.serasa.scale.Scale;
import br.com.serasa.serasa.scale.ScaleRepository;
import br.com.serasa.serasa.transport.TransactionStatus;
import br.com.serasa.serasa.transport.TransportTransactionRepository;
import br.com.serasa.serasa.transport.TransportTransactionRepository.BranchAggregate;
import br.com.serasa.serasa.transport.TransportTransactionRepository.GrainTypeAggregate;
import br.com.serasa.serasa.transport.TransportTransactionRepository.ScaleAggregate;
import br.com.serasa.serasa.transport.TransportTransactionRepository.StatusCount;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private static final BigDecimal KG_PER_TON = BigDecimal.valueOf(1000);

    private final TransportTransactionRepository transactionRepository;
    private final GrainTypeRepository grainTypeRepository;
    private final BranchRepository branchRepository;
    private final ScaleRepository scaleRepository;
    private final GrainPricingService pricingService;

    public ReportService(
            TransportTransactionRepository transactionRepository,
            GrainTypeRepository grainTypeRepository,
            BranchRepository branchRepository,
            ScaleRepository scaleRepository,
            GrainPricingService pricingService
    ) {
        this.transactionRepository = transactionRepository;
        this.grainTypeRepository = grainTypeRepository;
        this.branchRepository = branchRepository;
        this.scaleRepository = scaleRepository;
        this.pricingService = pricingService;
    }

    public List<GrainTypeReportItem> grainTypeReport() {
        Map<Long, GrainTypeAggregate> aggregates = transactionRepository
                .aggregateByGrainType(TransactionStatus.COMPLETED).stream()
                .collect(Collectors.toMap(GrainTypeAggregate::getGrainTypeId, Function.identity()));

        return grainTypeRepository.findAll().stream()
                .map(grainType -> toGrainTypeReportItem(grainType, aggregates.get(grainType.getId())))
                .toList();
    }

    public List<BranchReportItem> branchReport() {
        Map<Long, BranchAggregate> aggregates = transactionRepository
                .aggregateByBranch(TransactionStatus.COMPLETED).stream()
                .collect(Collectors.toMap(BranchAggregate::getBranchId, Function.identity()));

        return branchRepository.findAll().stream()
                .map(branch -> toBranchReportItem(branch, aggregates.get(branch.getId())))
                .toList();
    }

    public List<ScaleReportItem> scaleReport() {
        Map<String, ScaleAggregate> aggregates = transactionRepository.aggregateByScale().stream()
                .collect(Collectors.toMap(ScaleAggregate::getScaleCode, Function.identity()));

        return scaleRepository.findAll().stream()
                .map(scale -> toScaleReportItem(scale, aggregates.get(scale.getCode())))
                .toList();
    }

    public OverviewReport overview() {
        Map<TransactionStatus, Long> byStatus = transactionRepository.countByStatus().stream()
                .collect(Collectors.toMap(StatusCount::getStatus, StatusCount::getCount));
        long total = byStatus.values().stream().mapToLong(Long::longValue).sum();

        List<GrainTypeReportItem> grainTypes = grainTypeReport();
        BigDecimal totalCargoCost = grainTypes.stream()
                .map(GrainTypeReportItem::totalCargoCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalNetWeightTons = grainTypes.stream()
                .map(GrainTypeReportItem::totalNetWeightTons)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new OverviewReport(total, byStatus, totalCargoCost, totalNetWeightTons);
    }

    private GrainTypeReportItem toGrainTypeReportItem(GrainType grainType, GrainTypeAggregate aggregate) {
        BigDecimal salePricePerTon = pricingService.salePricePerTon(grainType);
        BigDecimal marginPercentage = pricingService.marginPercentage(grainType);
        BigDecimal potentialProfit = grainType.getAvailableQuantityTons()
                .multiply(salePricePerTon.subtract(grainType.getPurchasePricePerTon()))
                .setScale(2, RoundingMode.HALF_UP);

        return new GrainTypeReportItem(
                grainType.getId(),
                grainType.getName(),
                aggregate != null ? aggregate.getCompletedTransactions() : 0L,
                toTons(aggregate != null ? aggregate.getTotalNetWeightKg() : BigDecimal.ZERO),
                aggregate != null ? aggregate.getTotalCargoCost() : BigDecimal.ZERO,
                grainType.getAvailableQuantityTons(),
                marginPercentage,
                salePricePerTon,
                potentialProfit
        );
    }

    private BranchReportItem toBranchReportItem(Branch branch, BranchAggregate aggregate) {
        return new BranchReportItem(
                branch.getId(),
                branch.getName(),
                aggregate != null ? aggregate.getCompletedTransactions() : 0L,
                toTons(aggregate != null ? aggregate.getTotalNetWeightKg() : BigDecimal.ZERO),
                aggregate != null ? aggregate.getTotalCargoCost() : BigDecimal.ZERO
        );
    }

    private ScaleReportItem toScaleReportItem(Scale scale, ScaleAggregate aggregate) {
        BigDecimal averageSeconds = aggregate != null && aggregate.getAverageStabilizationSeconds() != null
                ? BigDecimal.valueOf(aggregate.getAverageStabilizationSeconds()).setScale(1, RoundingMode.HALF_UP)
                : null;
        return new ScaleReportItem(
                scale.getCode(),
                scale.getBranch().getName(),
                aggregate != null ? aggregate.getCompletedWeighings() : 0L,
                averageSeconds
        );
    }

    private BigDecimal toTons(BigDecimal kg) {
        return kg.divide(KG_PER_TON, 3, RoundingMode.HALF_UP);
    }
}