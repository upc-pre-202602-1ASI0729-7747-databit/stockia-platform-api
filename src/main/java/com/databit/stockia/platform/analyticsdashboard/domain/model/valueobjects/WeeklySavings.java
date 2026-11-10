package com.databit.stockia.platform.analyticsdashboard.domain.model.valueobjects;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Weekly savings summary (C4: Weekly Savings Calculator). All amounts in soles. "Rescued" is stock used
 * by sales during its last days before expiring; "waste" is stock that expired without being used.
 */
public record WeeklySavings(LocalDate from, LocalDate to, BigDecimal estimatedSavings, BigDecimal rescuedValue,
                            BigDecimal previousRescuedValue, BigDecimal wasteValue, BigDecimal previousWasteValue,
                            BigDecimal wasteReduction, int confirmedSales, int portionsSold, BigDecimal revenue,
                            BigDecimal previousRevenue, int projectedUnitsNextWeek, BigDecimal forecastConfidence,
                            List<SavingsLine> topRescued, List<SavingsLine> wasted) {
}
