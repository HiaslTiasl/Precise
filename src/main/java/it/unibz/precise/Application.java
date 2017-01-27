package it.unibz.precise;

import java.util.Locale;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.support.ErrorPageFilter;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

/**
 * Main class of Spring app.
 * Configures and starts the application.
 * 
 * @author Matthias
 * @see ConsistencyConfig
 * @see RepositoryConfiguration
 * @see WebConfig
 *
 */
@SpringBootApplication
@ComponentScan("it.unibz.precise")	
@EntityScan("it.unibz.precise.model")		
@EnableJpaRepositories("it.unibz.precise.rep")	
public class Application extends SpringBootServletInitializer {
	
	/** Entry point. */
	public static void main(String[] args) {
		// Disable auto-restart on file changes, which is disturbing for small changes,
		// in particular for changes in static web content (HTML, CSS, JS), which actually
		// should not trigger a restart, but it does, at least when running in an external
		// Tomcat.
		System.setProperty("spring.devtools.restart.enabled", "false");
		SpringApplication.run(Application.class, args);
	}
	
	/** Tell Spring about other classes with further configuration. */
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Application.class, ResourceProcessorConfig.class, ConsistencyConfig.class);
	}
	
	/**
	 * For the time being, fix the locale to avoid mixed languages depending on the user's browser settings.
	 * TODO: When later multiple languages are considered, check whether this should be removed. 
	 */
	@Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.US);
        return slr;
    }
	
	/**
	 * Disable default ErrorPageFilter.
	 * @see #disableSpringBootErrorFilter(ErrorPageFilter)
	 * @see <a href="http://stackoverflow.com/questions/30170586/how-to-disable-errorpagefilter-in-spring-boot/31858680">
	 * 	http://stackoverflow.com/questions/30170586/how-to-disable-errorpagefilter-in-spring-boot/31858680
	 * </a>
	 */
	@Bean
    public ErrorPageFilter errorPageFilter() {
        return new ErrorPageFilter();
    }

	/**
	 * Disable default ErrorPageFilter.
	 * @see #disableSpringBootErrorFilter(ErrorPageFilter)
	 * @see <a href="http://stackoverflow.com/questions/30170586/how-to-disable-errorpagefilter-in-spring-boot/31858680">
	 * 	http://stackoverflow.com/questions/30170586/how-to-disable-errorpagefilter-in-spring-boot/31858680
	 * </a>
	 */
    @Bean
    public FilterRegistrationBean disableSpringBootErrorFilter(ErrorPageFilter filter) {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(filter);
        filterRegistrationBean.setEnabled(false);
        return filterRegistrationBean;
    }
	
}