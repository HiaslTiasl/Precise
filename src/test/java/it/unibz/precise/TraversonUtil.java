package it.unibz.precise;

import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.springframework.hateoas.LinkDiscoverer;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.Traverson;
import org.springframework.hateoas.hal.HalLinkDiscoverer;
import org.springframework.test.web.servlet.MvcResult;

public class TraversonUtil {
	
	private static LinkDiscoverer linkDiscoverer = new HalLinkDiscoverer();
	
	public static Traverson create(String baseURI) {
		return new Traverson(URI.create(baseURI), MediaTypes.HAL_JSON);
	}
	
	public static Traverson continueFrom(MvcResult result) throws UnsupportedEncodingException {
		return continueFrom(result.getResponse().getContentAsString());
	}
	
	public static Traverson continueFrom(String json) {
		return create(linkDiscoverer.findLinkWithRel("self", json).getHref());
	}

}
