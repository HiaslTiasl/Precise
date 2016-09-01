package it.unibz.precise;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import it.unibz.precise.model.Dependency;
import it.unibz.precise.model.Task;
import it.unibz.precise.model.projection.DependencySummaryProjection;
import it.unibz.precise.model.projection.ExpandedTaskProjection;
import it.unibz.precise.rest.DependencyLinks;
import it.unibz.precise.rest.TaskLinks;

@SpringBootApplication
//@EnableJpaRepositories("it.unibz.precise")	
//@ComponentScan("it.unibz.precise")	
//@EntityScan("it.unibz.precise")		
public class Application extends SpringBootServletInitializer {
	
	@Autowired
	private TaskLinks taskLinks;
	@Autowired
	private DependencyLinks dependencyLinks;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Application.class);
	}
	
	@Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.US);
        return slr;
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