package it.unibz.precise;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Web-specific configurations.
 * 
 * @author MatthiasP
 *
 */
@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

	/**
	 * Do not use path extensions for determining file content,
	 * so MDL files are checked by syntax not by content type.
	 */
	@Override
	public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
		configurer.favorPathExtension(false);
	}
	
}
