package com.databit.stockia.platform.mlrecommendations.infrastructure;

import com.databit.stockia.platform.mlrecommendations.domain.services.DemandFeatureBuilder;
import com.databit.stockia.platform.mlrecommendations.infrastructure.machinelearning.OnnxDemandModel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * The Java feature builder and the ONNX Runtime prediction must match the Python training pipeline
 * (ml/models/parity_fixture.json).
 */
class DemandModelParityTest {
    private static JsonNode fixture;

    @BeforeAll
    static void loadFixture() throws IOException {
        try (var stream = DemandModelParityTest.class.getResourceAsStream("/ml/parity_fixture.json")) {
            fixture = new ObjectMapper().readTree(stream);
        }
    }

    private static float[] featuresOf(JsonNode testCase) {
        var history = new HashMap<LocalDate, Double>();
        testCase.get("history").properties().forEach(entry -> history.put(LocalDate.parse(entry.getKey()), entry.getValue().asDouble()));
        return DemandFeatureBuilder.build(history, LocalDate.parse(testCase.get("date").asText()), testCase.get("temp_max_c").asDouble(),
                testCase.get("precipitation_mm").asDouble());
    }

    @Test
    void featureOrderMatchesTheTrainingPipeline() {
        var order = new ArrayList<String>();
        fixture.get("feature_order").forEach(item -> order.add(item.asText()));
        assertThat(order).isEqualTo(DemandFeatureBuilder.FEATURE_ORDER);
    }

    @Test
    void featuresMatchTheTrainingPipeline() {
        for (var testCase : fixture.get("cases")) {
            var features = featuresOf(testCase);
            var expected = testCase.get("features");
            for (var index = 0; index < features.length; index++)
                assertThat((double) features[index]).as("%s feature %s", testCase.get("date").asText(), DemandFeatureBuilder.FEATURE_ORDER.get(index))
                        .isCloseTo(expected.get(index).asDouble(), within(1e-4));
        }
    }

    @Test
    void onnxPredictionsMatchTheTrainingPipeline() throws Exception {
        var model = new OnnxDemandModel(new DefaultResourceLoader(), new ObjectMapper(), "classpath:ml/");
        assertThat(model.isReady()).as(model.info().problem()).isTrue();
        List<float[]> rows = new ArrayList<>();
        fixture.get("cases").forEach(testCase -> rows.add(featuresOf(testCase)));

        var predictions = model.predict(rows);

        assertThat(predictions).hasSize(rows.size());
        var index = 0;
        for (var testCase : fixture.get("cases"))
            assertThat((double) predictions[index++]).isCloseTo(testCase.get("prediction").asDouble(), within(1e-3));
        model.close();
    }
}
