package com.ing.interview.metalnotifier.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetalPrice {
    @NotBlank(message = "Typ metalu nie może być pusty")
    @Pattern(regexp = "gold|silver|platinum", message = "Typ metalu musi być jednym z: gold, silver, platinum")
    private String itemType;

    @NotNull(message = "Cena nie może być pusta")
    @DecimalMin(value = "0.0", message = "Cena nie może być ujemna")
    @Digits(integer = 10, fraction = 2, message = "Cena musi mieć maksymalnie 2 miejsca po przecinku")
    private BigDecimal price;
}