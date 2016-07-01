package it.unibz.precise;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

public class TestUtil {
	
	public static String load(String resource) throws IOException {
		try (InputStream is = new ClassPathResource("nestedModel.json").getInputStream()) {
			return StreamUtils.copyToString(is, StandardCharsets.UTF_8);
		}
	}

}
