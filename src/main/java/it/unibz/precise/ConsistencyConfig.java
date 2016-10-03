package it.unibz.precise;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import it.unibz.precise.check.SCCFinder;
import it.unibz.precise.check.SCCTarjan;

@Configuration
public class ConsistencyConfig {
	
	@Bean
	public SCCFinder sccFinder() {
		return new SCCTarjan();
	}

}
