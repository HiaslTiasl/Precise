package it.unibz.precise;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import it.unibz.precise.check.SCCFinder;
import it.unibz.precise.graph.SCCTarjan;

/**
 * Configuration specific to consistency checking.
 * 
 * @author MatthiasP
 *
 */
@Configuration
public class ConsistencyConfig {
	
	/** Implementation of SCCFinder. */
	@Bean
	public SCCFinder sccFinder() {
		return new SCCTarjan();
	}

}
