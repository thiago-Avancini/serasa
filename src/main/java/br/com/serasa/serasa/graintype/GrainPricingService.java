package br.com.serasa.serasa.graintype;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

/**
 * Sale margin is inversely proportional to how much of a grain type is currently
 * available at the dock: scarce stock pushes the margin toward the max, abundant
 * stock pulls it toward the min. {@code referenceQuantityTons} is the stock level
 * considered "full" (margin bottoms out at {@code min} from that point on).
 */
@Service
public class GrainPricingService {

    private final MarginProperties properties;

    public GrainPricingService(MarginProperties properties) {
        this.properties = properties;
    }

    public BigDecimal marginPercentage(GrainType grainType) {
        BigDecimal ratio = grainType.getAvailableQuantityTons()
                .divide(properties.referenceQuantityTons(), 6, RoundingMode.HALF_UP)
                .min(BigDecimal.ONE)
                .max(BigDecimal.ZERO);
        BigDecimal spread = properties.max().subtract(properties.min());
        return properties.max().subtract(spread.multiply(ratio)).setScale(4, RoundingMode.HALF_UP);
    }

    public BigDecimal salePricePerTon(GrainType grainType) {
        BigDecimal margin = marginPercentage(grainType);
        return grainType.getPurchasePricePerTon()
                .multiply(BigDecimal.ONE.add(margin))
                .setScale(2, RoundingMode.HALF_UP);
    }
}