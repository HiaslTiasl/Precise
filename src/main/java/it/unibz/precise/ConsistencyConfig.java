package it.unibz.precise;

import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import it.unibz.precise.check.ConsistencyChecker;
import it.unibz.precise.check.ConsistencyWarning;
import it.unibz.precise.check.CycleChecker;
import it.unibz.precise.check.OverlappingLocationsChecker;
import it.unibz.precise.check.SCCFinder;
import it.unibz.precise.check.SCCTarjan;
import it.unibz.precise.model.Model;

@Configuration
public class ConsistencyConfig {
	
	@Autowired
	private CycleChecker cycleChecker;
	@Autowired
	private OverlappingLocationsChecker overlappingChecker;
	
	@Bean
	public SCCFinder sccFinder() {
		return new SCCTarjan();
	}

	@Bean
	@Primary
	public ConsistencyChecker consistencyChecker() {
		return new ConsistencyChecker() {
			@Override
			public Stream<ConsistencyWarning> check(Model model) {
				return Stream.of(cycleChecker, overlappingChecker)
					.flatMap(c -> c.check(model));
			}
		};
	}

}
