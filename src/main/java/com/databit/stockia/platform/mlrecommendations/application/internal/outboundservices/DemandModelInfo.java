package com.databit.stockia.platform.mlrecommendations.application.internal.outboundservices;

import java.util.List;

/**
 * Information about the loaded model (GET /demand-forecasts/model). metrics is the JSON object of the
 * model metadata.
 */
public record DemandModelInfo(boolean ready, String modelVersion, String trainedAt, String algorithm, List<String> dataSources,
                              double confidenceBase, Object metrics, String problem) {
}
