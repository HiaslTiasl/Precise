package it.unibz.precise;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import it.unibz.precise.model.Task;

/**
 * Spring Data REST specific configuration
 * 
 * @author MatthiasP
 *
 */
@Configuration
public class RepositoryConfiguration extends RepositoryRestConfigurerAdapter {

	/** Include IDs of tasks in responses, because they are shown in boxes. */
    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
    	// The task ID must be shown in the diagram
        config.exposeIdsFor(Task.class);
    }
    
    /** Make ProjectionFactory injectable. */
    @Bean
    public ProjectionFactory projectionFactory() {
    	return new SpelAwareProxyProjectionFactory();
    }
    
    /**
     * Create a validator to use in bean validation - primary to be able to autowire without qualifier
     */
    @Bean
    @Primary
    Validator validator() {
        return new LocalValidatorFactoryBean();
    }

    /** Trigger Bean validation whenever entities are created or updated. */
    @Override
    public void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener validatingListener) {
        Validator validator = validator();
        //bean validation always before save and create
        validatingListener.addValidator("beforeCreate", validator);
        validatingListener.addValidator("beforeSave", validator);
    }
    
}