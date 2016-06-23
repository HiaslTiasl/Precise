package it.unibz.precise.main;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;

@Provider
public class JacksonProvider implements ContextResolver<ObjectMapper> {
 
    private final ObjectMapper defaultObjectMapper;
    
    public JacksonProvider() {
    	defaultObjectMapper = new ObjectMapper();
		//AnnotationIntrospector primary = new JacksonAnnotationIntrospector();
		//AnnotationIntrospector secondary = new JaxbAnnotationIntrospector(defaultObjectMapper.getTypeFactory());
		//AnnotationIntrospector pair = AnnotationIntrospector.pair(primary, secondary);
		//defaultObjectMapper.setAnnotationIntrospector(pair);
	}
 
    @Override
    public ObjectMapper getContext(Class<?> type) {
        return defaultObjectMapper;
    }
 
}
