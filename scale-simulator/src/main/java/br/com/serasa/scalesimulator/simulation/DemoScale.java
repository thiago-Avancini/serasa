package br.com.serasa.scalesimulator.simulation;

import br.com.serasa.scalesimulator.client.dto.GrainTypeDto;
import br.com.serasa.scalesimulator.client.dto.TruckDto;
import java.util.List;

/**
 * A simulated scale and the pool of trucks/grain types it draws from. The truck pool is a
 * queue-like simulation of several trucks passing through the same physical scale over time
 * — a new one is picked at random for every weighing cycle — while remaining disjoint from
 * every other scale's pool, since a truck can't be on two scales at once.
 */
public record DemoScale(String scaleCode, Long branchId, List<TruckDto> trucks, List<GrainTypeDto> grainTypes) {
}