package br.com.serasa.serasa.reading;

import br.com.serasa.serasa.common.PlateNormalizer;
import br.com.serasa.serasa.common.exception.NotFoundException;
import br.com.serasa.serasa.scale.ScaleRepository;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

@Service
public class ReadingIngestionService {

    private final ScaleRepository scaleRepository;
    private final ScaleStabilizationService stabilizationService;

    public ReadingIngestionService(ScaleRepository scaleRepository, ScaleStabilizationService stabilizationService) {
        this.scaleRepository = scaleRepository;
        this.stabilizationService = stabilizationService;
    }

    public void ingest(String scaleCode, String plate, BigDecimal weightKg) {
        if (!scaleRepository.existsById(scaleCode)) {
            throw new NotFoundException("Scale '" + scaleCode + "' not found");
        }
        stabilizationService.process(scaleCode, PlateNormalizer.normalize(plate), weightKg);
    }
}