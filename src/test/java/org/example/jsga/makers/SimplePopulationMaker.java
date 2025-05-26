package org.example.jsga.makers;

import com.natpryce.makeiteasy.Instantiator;
import com.natpryce.makeiteasy.Maker;
import com.natpryce.makeiteasy.Property;
import org.example.jsga.model.Individual;
import org.example.jsga.model.Population;
import org.example.jsga.model.SimpleIndividual;
import org.example.jsga.model.SimplePopulation;

import java.util.List;
import java.util.stream.Collectors;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.with;

public class SimplePopulationMaker {
    private static final Property<SimplePopulation, List<? extends Individual>> INDIVIDUALS = Property.newProperty();

    public static final Instantiator<SimplePopulation> INSTANTIATOR = lookup -> {
        SimplePopulation population = new SimplePopulation();
        population.setIndividuals((List<Individual>) lookup.valueOf(INDIVIDUALS, (List<Individual>) null));
        return population;
    };

    public static final Maker<SimplePopulation> VALID_SIMPLE_POPULATION = a(INSTANTIATOR);

    private SimplePopulationMaker() {}

    public static SimplePopulation makeSimplePopulation(final List<SimpleIndividual> individuals) {
        return VALID_SIMPLE_POPULATION.but(with(INDIVIDUALS, individuals)).make();
    }

    public static Population createPopulationWithGenes(List<byte[]> genes) {
        List<SimpleIndividual> individuals = genes.stream()
                .map(SimpleIndividualMaker::buildSimpleIndividualByByteArray)
                .collect(Collectors.toList());
        return SimplePopulationMaker.makeSimplePopulation(individuals);
    }
}
