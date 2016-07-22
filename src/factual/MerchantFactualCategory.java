package factual;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Joms on 7/11/2016.
 */
public class MerchantFactualCategory {
    private String merchantId;
    private List<String> factualCategoryIds;
    private List<String> primaryFactualCategoryLabel;

    public MerchantFactualCategory(String merchantId, List<String> factualCategoryIds, List<String> primaryFactualCategoryLabel) {
        this.merchantId = merchantId;
        this.factualCategoryIds = factualCategoryIds;
        this.primaryFactualCategoryLabel = primaryFactualCategoryLabel;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public List<String> getFactualCategoryIds() {
        return factualCategoryIds;
    }

    public List<String> getPrimaryFactualCategoryLabel() {
        return primaryFactualCategoryLabel;
    }

    public boolean hasDiscrepantStores() {
        return factualCategoryIds.size()>1;
    }

    public static List<String> preprocessFactualCatLabels(List<String> labels) {
        Set<String> allWords = new LinkedHashSet<>();
        for (String label : labels) {
            String[] words = label.split(",|\\sand\\s");
            allWords.addAll(Arrays.stream(words)
                    .map(w -> w.toLowerCase())
                    .collect(Collectors.toList()));
        }

        return (List<String>) allWords;
    }

    @Override
    public String toString() {
        return "MerchantFactualCategory{" +
                "merchantId=" + merchantId +
                ", factualCategoryIds=" + factualCategoryIds +
                ", primaryFactualCategoryLabel=" + primaryFactualCategoryLabel +
                '}';
    }
}
