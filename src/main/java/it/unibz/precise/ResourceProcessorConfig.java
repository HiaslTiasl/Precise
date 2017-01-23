package it.unibz.precise;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.ResourceProcessor;

import it.unibz.precise.model.Dependency;
import it.unibz.precise.model.Model;
import it.unibz.precise.model.Task;
import it.unibz.precise.model.projection.DependencySummaryProjection;
import it.unibz.precise.model.projection.ExpandedTaskProjection;
import it.unibz.precise.model.projection.ModelSummaryProjection;
import it.unibz.precise.rest.AbstractDependencyResourceProcessor;
import it.unibz.precise.rest.AbstractModelResourceProcessor;
import it.unibz.precise.rest.AbstractTaskResourceProcessor;

/**
 * Custom {@link ResourceProcessor}s.
 * 
 * Link to custom REST controllers from entities and their projections.
 * 
 * @author MatthiasP
 *
 */
@Configuration
public class ResourceProcessorConfig {
	
	/** Add custom links to {@link Model}s. */
	@Bean
	public ModelResourceProcessor modelProcessor() {
		return new ModelResourceProcessor();
	}
	
	/** Add custom links to {@link Model}s. */
	@Bean
	public ModelSummaryResourceProcessor modelSummaryProcessor() {
		return new ModelSummaryResourceProcessor();
	}

	/** Add custom links to {@link Task}s. */
	@Bean
	public TaskResourceProcessor taskProcessor() {
		return new TaskResourceProcessor();
	}
	
	/** Add custom links to {@link Task}s. */
	@Bean
	public ExpandedTaskResourceProcessor expandedTaskProcessor() {
		return new ExpandedTaskResourceProcessor();
	}
	
	/** Add custom links to {@link Dependency}s. */
	@Bean
	public DependencyResourceProcessor dependencyProcessor() {
		return new DependencyResourceProcessor();
	}
	
	/** Add custom links to {@link Dependency}s. */
	@Bean
	public DependencySummaryResourceProcessor dependencySummaryProcessor() {
		return new DependencySummaryResourceProcessor();
	}
	
	// Concrete classes for entities and projections
	//
	// Required because Spring does not seem to pick processors based on .getClass() of the returned value,
	// rather than the declared return type.
	// Thus, changing e.g. AbstractTaskResourceProcessor to a concrete class and instantiating it with <Task>
	// does not work.
	
	public static class ModelResourceProcessor extends AbstractModelResourceProcessor<Model> {}
	public static class ModelSummaryResourceProcessor extends AbstractModelResourceProcessor<ModelSummaryProjection> {}
	
	public static class TaskResourceProcessor extends AbstractTaskResourceProcessor<Task> {}
	public static class ExpandedTaskResourceProcessor extends AbstractTaskResourceProcessor<ExpandedTaskProjection> {}
	
	public static class DependencyResourceProcessor extends AbstractDependencyResourceProcessor<Dependency> {}
	public static class DependencySummaryResourceProcessor extends AbstractDependencyResourceProcessor<DependencySummaryProjection> {}
	
}
