package com.databit.stockia.platform.mlrecommendations.domain.model.valueobjects;

public record RecommendationProposal(RecommendationType type, LocalizedMessage message, LocalizedMessage expectedImpact) {
}
