package it.unibz.precise;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;

import it.unibz.precise.model.Dependency;
import it.unibz.precise.model.Model;
import it.unibz.precise.model.Phase;
import it.unibz.precise.model.Task;
import it.unibz.precise.model.projection.DependencySummaryProjection;
import it.unibz.precise.model.projection.ExpandedTaskProjection;
import it.unibz.precise.model.projection.ModelSummaryProjection;
import it.unibz.precise.model.projection.PhaseSummaryProjection;
import it.unibz.precise.rest.DependencyLinks;
import it.unibz.precise.rest.ModelLinks;
import it.unibz.precise.rest.PhaseLinks;
import it.unibz.precise.rest.TaskLinks;

@Configuration
public class ResourceProcessorConfig {
	
	@Autowired
	private ModelLinks modelLinks;
	
	@Autowired
	private TaskLinks taskLinks;
	
	@Autowired
	private PhaseLinks phaseLinks;
	
	@Autowired
	private DependencyLinks dependencyLinks;
	
	@Bean
	public ResourceProcessor<Resource<Model>> modelProcessor() {
		return new ResourceProcessor<Resource<Model>>() {
			public Resource<Model> process(Resource<Model> resource) {
				return modelLinks.withCustomLinks(resource, r -> r.getContent().getId());
			}
		};
	}

	@Bean
	public ResourceProcessor<Resource<ModelSummaryProjection>> modelSummaryProcessor() {
		return new ResourceProcessor<Resource<ModelSummaryProjection>>() {
			public Resource<ModelSummaryProjection> process(Resource<ModelSummaryProjection> resource) {
				return modelLinks.withCustomLinks(resource, r -> r.getContent().getId());
			}
		};
	}
	
	@Bean
	public ResourceProcessor<Resource<Task>> taskProcessor() {
		return new ResourceProcessor<Resource<Task>>() {
			public Resource<Task> process(Resource<Task> resource) {
				return taskLinks.withCustomLinks(resource, r -> r.getContent().getId());
			}
		};
	}
	
	@Bean
	public ResourceProcessor<Resource<ExpandedTaskProjection>> expandedTaskProcessor() {
		return new ResourceProcessor<Resource<ExpandedTaskProjection>>() {
			public Resource<ExpandedTaskProjection> process(Resource<ExpandedTaskProjection> resource) {
				return taskLinks.withCustomLinks(resource, r -> r.getContent().getId());
			}
		};
	}
	
	@Bean
	public ResourceProcessor<Resource<Phase>> phaseProcessor() {
		return new ResourceProcessor<Resource<Phase>>() {
			public Resource<Phase> process(Resource<Phase> resource) {
				return phaseLinks.withCustomLinks(resource, r -> r.getContent().getId());
			}
		};
	}
	
	@Bean
	public ResourceProcessor<Resource<PhaseSummaryProjection>> phaseSummaryProcessor() {
		return new ResourceProcessor<Resource<PhaseSummaryProjection>>() {
			public Resource<PhaseSummaryProjection> process(Resource<PhaseSummaryProjection> resource) {
				return phaseLinks.withCustomLinks(resource, r -> r.getContent().getId());
			}
		};
	}
	
	@Bean
	public ResourceProcessor<Resource<Dependency>> dependencyProcessor() {
		return new ResourceProcessor<Resource<Dependency>>() {
			public Resource<Dependency> process(Resource<Dependency> resource) {
				return dependencyLinks.withCustomLinks(resource, r -> r.getContent().getId());
			}
		};
	}
	
	@Bean
	public ResourceProcessor<Resource<DependencySummaryProjection>> dependencySummaryProcessor() {
		return new ResourceProcessor<Resource<DependencySummaryProjection>>() {
			public Resource<DependencySummaryProjection> process(Resource<DependencySummaryProjection> resource) {
				return dependencyLinks.withCustomLinks(resource, r -> r.getContent().getId());
			}
		};
	}

}
