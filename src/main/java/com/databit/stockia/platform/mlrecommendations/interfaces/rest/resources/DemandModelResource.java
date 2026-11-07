package com.databit.stockia.platform.mlrecommendations.interfaces.rest.resources;

import java.util.List;

/**
 * Version, metrics and data sources of the model in use.
 */
public record DemandModelResource(boolean ready, String modelVersion, String trainedAt, String algorithm, List<String> dataSources,
                                  double confidenceBase, Object metrics, String problem, List<String> featureOrder, int minimumHistoryDays) {
}
