package com.databit.stockia.platform.mlrecommendations.infrastructure.machinelearning;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import com.databit.stockia.platform.mlrecommendations.application.internal.outboundservices.DemandModel;
import com.databit.stockia.platform.mlrecommendations.application.internal.outboundservices.DemandModelInfo;
import com.databit.stockia.platform.mlrecommendations.domain.services.DemandFeatureBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Loads demand_model.onnx and its metadata once (singleton) with ONNX Runtime for Java. When the files
 * are missing or the feature order differs from DemandFeatureBuilder.FEATURE_ORDER the model stays
 * "not ready" and forecasts use the baseline.
 */
@Slf4j
@Component
public class OnnxDemandModel implements DemandModel {
    private final OrtEnvironment environment;
    private final OrtSession session;
    private final DemandModelInfo info;
    private String inputName = "features";
    private String outputName = "units";

    public OnnxDemandModel(ResourceLoader resourceLoader, ObjectMapper objectMapper,
                           @Value("${stockia.ml.model-location:classpath:ml/}") String location) {
        var folder = location.endsWith("/") ? location : location + "/";
        OrtEnvironment ortEnvironment = null;
        OrtSession ortSession = null;
        JsonNode metadata = objectMapper.createObjectNode();
        String problem = null;
        try {
            var modelResource = resourceLoader.getResource(folder + "demand_model.onnx");
            var metadataResource = resourceLoader.getResource(folder + "model_metadata.json");
            if (!modelResource.exists() || !metadataResource.exists()) throw new IllegalStateException("Model files not found in " + folder);
            try (var stream = metadataResource.getInputStream()) {
                metadata = objectMapper.readTree(stream);
            }
            var featureOrder = new ArrayList<String>();
            metadata.required("feature_order").forEach(item -> featureOrder.add(item.asText()));
            if (!featureOrder.equals(DemandFeatureBuilder.FEATURE_ORDER))
                throw new IllegalStateException("The model feature order does not match DemandFeatureBuilder.FEATURE_ORDER.");
            if (metadata.hasNonNull("onnx_input")) inputName = metadata.get("onnx_input").asText();
            if (metadata.hasNonNull("onnx_output")) outputName = metadata.get("onnx_output").asText();
            byte[] model;
            try (var stream = modelResource.getInputStream()) {
                model = stream.readAllBytes();
            }
            ortEnvironment = OrtEnvironment.getEnvironment();
            ortSession = ortEnvironment.createSession(model, new OrtSession.SessionOptions());
            log.info("Demand model {} loaded from {}", metadata.path("model_version").asText(), folder);
        } catch (Exception | UnsatisfiedLinkError exception) {
            problem = exception.getMessage();
            log.error("The demand model could not be loaded; forecasts will use the baseline", exception);
        }
        this.environment = ortEnvironment;
        this.session = ortSession;
        var sources = new ArrayList<String>();
        metadata.path("data_sources").forEach(item -> sources.add(item.asText()));
        this.info = new DemandModelInfo(session != null, metadata.path("model_version").asText("none"), metadata.path("trained_at").asText(""),
                metadata.path("algorithm").asText(""), sources, metadata.path("confidence_base").asDouble(0.5),
                metadata.has("metrics") ? metadata.get("metrics") : null, problem);
    }

    @Override
    public boolean isReady() {
        return session != null;
    }

    @Override
    public DemandModelInfo info() {
        return info;
    }

    @Override
    public float[] predict(List<float[]> rows) {
        if (session == null) throw new IllegalStateException("The demand model is not loaded.");
        if (rows.isEmpty()) return new float[0];
        var columns = DemandFeatureBuilder.FEATURE_ORDER.size();
        var data = new float[rows.size() * columns];
        for (var row = 0; row < rows.size(); row++) System.arraycopy(rows.get(row), 0, data, row * columns, columns);
        try (var tensor = OnnxTensor.createTensor(environment, FloatBuffer.wrap(data), new long[]{rows.size(), columns});
             var results = session.run(Map.of(inputName, tensor), Set.of(outputName))) {
            return flatten(results.get(0).getValue(), rows.size());
        } catch (OrtException exception) {
            throw new IllegalStateException("The demand model failed: " + exception.getMessage(), exception);
        }
    }

    private static float[] flatten(Object value, int rows) {
        if (value instanceof float[] vector) return vector;
        if (value instanceof float[][] matrix) {
            var output = new float[rows];
            for (var row = 0; row < rows; row++) output[row] = matrix[row][0];
            return output;
        }
        if (value instanceof double[] vector) {
            var output = new float[vector.length];
            for (var index = 0; index < vector.length; index++) output[index] = (float) vector[index];
            return output;
        }
        throw new IllegalStateException("Unexpected model output " + value.getClass());
    }

    @PreDestroy
    public void close() throws OrtException {
        if (session != null) session.close();
    }
}
