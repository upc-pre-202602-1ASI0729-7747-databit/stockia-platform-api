package com.databit.stockia.platform.analyticsdashboard.domain.model.valueobjects;

import java.math.BigDecimal;

/**
 * Sales of a period (input of the Weekly Savings Calculator).
 */
public record SalesTotals(int confirmedSales, int portionsSold, BigDecimal revenue) {
}