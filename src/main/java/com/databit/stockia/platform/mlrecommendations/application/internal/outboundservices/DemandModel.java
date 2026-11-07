package com.databit.stockia.platform.mlrecommendations.application.internal.outboundservices;

import java.util.List;

/**
 * The demand model: one row of features per dish and day (see DemandFeatureBuilder) to expected units.
 * Implemented with ONNX Runtime.
 */
public interface DemandModel {
    boolean isReady();

    DemandModelInfo info();

    float[] predict(List<float[]> rows);
}
