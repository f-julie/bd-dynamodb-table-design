package com.amazon.ata.dynamodbtabledesign.dao;

import com.amazon.ata.dynamodbtabledesign.model.Carton;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.inject.Inject;

/**
 * Provides access to cartons of ice cream in storage.
 */
public class CartonDao {
    private static final List<String> CARTONS = ImmutableList.of("Vanilla", "Strawberry");
    private static final List<String> EMPTY_CARTONS = ImmutableList.of("Chocolate");

    @Inject
    public CartonDao() {
    }

    public List<Carton> getCartonsByFlavorNames(List<String> flavorNames) {
        return flavorNames.stream()
            .map(name -> {
                if (CARTONS.contains(name)) {
                    return Carton.getCarton(name);
                } else if (EMPTY_CARTONS.contains(name)) {
                    return Carton.getEmptyCarton(name);
                }
                return null;
            })
            .filter(carton -> !Objects.isNull(carton))
            .collect(Collectors.toList());
    }
}
