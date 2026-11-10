package com.databit.stockia.platform.analyticsdashboard.interfaces.rest.resources;

import java.math.BigDecimal;
import java.util.List;

/**
 * Weekly savings (soles). from/to as yyyy-MM-dd in the restaurant time zone.
 */
public record WeeklySavingsResource(String from, String to, BigDecimal estimatedSavings, BigDecimal rescuedValue, BigDecimal previousRescuedValue,
                                    BigDecimal wasteValue, BigDecimal previousWasteValue, BigDecimal wasteReduction, int confirmedSales,
                                    int portionsSold, BigDecimal revenue, BigDecimal previousRevenue, int projectedUnitsNextWeek,
                                    BigDecimal forecastConfidence, List<SavingsLineResource> topRescued, List<SavingsLineResource> wasted) {
}
