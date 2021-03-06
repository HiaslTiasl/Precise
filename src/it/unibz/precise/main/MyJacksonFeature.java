package it.unibz.precise.main;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import org.glassfish.jersey.CommonProperties;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.fasterxml.jackson.jaxrs.base.JsonMappingExceptionMapper;
import com.fasterxml.jackson.jaxrs.base.JsonParseExceptionMapper;

/** http://stackoverflow.com/a/20713200 */
public class MyJacksonFeature implements Feature{

	@Override
	public boolean configure(final FeatureContext context ) {

		String postfix = '.' + context.getConfiguration().getRuntimeType().name().toLowerCase();
		
		context.property(CommonProperties.MOXY_JSON_FEATURE_DISABLE/* + postfix*/, true);
		
		context.register(JsonParseExceptionMapper.class);
		context.register(JsonMappingExceptionMapper.class);
		context.register(JacksonJsonProvider.class, MessageBodyReader.class, MessageBodyWriter.class);
		
		return true;
	}

}
