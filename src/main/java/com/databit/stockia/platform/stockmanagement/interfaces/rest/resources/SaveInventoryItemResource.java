package com.databit.stockia.platform.stockmanagement.interfaces.rest.resources;

import com.databit.stockia.platform.shared.domain.model.valueobjects.Quantity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Body to create or edit a supply. Quantity and minThreshold accept a JSON number or a string with
 * up to 32 decimals. The expiration date is calculated by the server (today + shelfLifeDays).
 */
public record SaveInventoryItemResource(
        @NotBlank(message = "{validation.required}") @Size(max = 120, message = "{validation.maxLength}")
        @Schema(example = "Pechuga de pollo") String name,
        @NotBlank(message = "{validation.required}") @Size(max = 20, message = "{validation.maxLength}")
        @Schema(example = "kg") String unit,
        @NotNull(message = "{validation.required}") @Schema(type = "string", example = "12.5") Quantity quantity,
        @NotNull(message = "{validation.required}") @Schema(type = "string", example = "5") Quantity minThreshold,
        @NotBlank(message = "{validation.required}") @Schema(example = "REFRIGERATED", allowableValues = {"AMBIENT", "REFRIGERATED", "FROZEN"})
        String storageType,
        @Min(value = 1, message = "{validation.min}") @Max(value = 3650, message = "{validation.max}") @Schema(example = "4")
        int shelfLifeDays,
        @NotNull(message = "{validation.required}") @DecimalMin(value = "0", message = "{validation.min}")
        @DecimalMax(value = "1000000", message = "{validation.max}") @Schema(example = "14.5") BigDecimal unitCost) {
}
