package com.y271727uy.shopcore.core.algorithm.shapleyvalue;

import java.util.Set;

@FunctionalInterface
public interface CoalitionValueFunction<P> {
    double valueOf(Set<P> coalition);
}
