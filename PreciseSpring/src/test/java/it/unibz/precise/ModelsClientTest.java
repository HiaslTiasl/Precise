package it.unibz.precise;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.Traverson;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StreamUtils;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(Application.class)
@WebIntegrationTest(randomPort=true)
public class ModelsClientTest {

	private static final String SERVICE_URI = "http://localhost:%s";
	
	@Value("${local.server.port}")
	private int port;
	
	@Autowired
	private WebApplicationContext context;
	
	private MockMvc mockMvc;
	
	private Traverson traverson;
	
	@Before
	public void setup() {
		mockMvc = webAppContextSetup(this.context).build();
		traverson = new Traverson(URI.create(String.format(SERVICE_URI, port)), MediaTypes.HAL_JSON);
	}

	@Test
	public void postNestedModel() throws IOException, Exception {
		// Set up path traversal
		Link modelsLink = traverson.follow("models").asLink();
		try (InputStream is = new ClassPathResource("nestedModel.json").getInputStream()) {
			String json = StreamUtils.copyToString(is, StandardCharsets.UTF_8);
			Assert.assertThat(json, not(isEmptyOrNullString()));
			mockMvc.perform(
				post(modelsLink.getHref())
				.accept(MediaTypes.HAL_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json)
			)
			.andExpect(status().isCreated());
		}
		
	    // more assertions

	}

}
