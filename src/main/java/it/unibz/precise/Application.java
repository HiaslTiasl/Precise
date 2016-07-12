package it.unibz.precise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;

import it.unibz.precise.jackson.UniquePropertyPolymorphicDeserializer;
import it.unibz.precise.model.BinaryConstraint;
import it.unibz.precise.model.Constraint;
import it.unibz.precise.model.UnaryConstraint;

@SpringBootApplication
//@EnableJpaRepositories("it.unibz.precise.rest.rep")
//@ComponentScan("it.unibz.precise.rest")
//@EntityScan("it.unibz.precise.model")
public class Application extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Application.class);
	}
	
	@Bean
	@SuppressWarnings("rawtypes")
	public Module constraintDeserializerModule() {
		SimpleModule module = new SimpleModule(
			"ConstraintDeserializationModule",
			new Version(1, 0, 0, "SNAPSHOT", "Precise", "Presice")
		);
		UniquePropertyPolymorphicDeserializer<Constraint> deserializer =
				new UniquePropertyPolymorphicDeserializer<>(Constraint.class);
		deserializer.register(UnaryConstraint.TASK_FIELD_NAME, UnaryConstraint.class);
		deserializer.registerDefault(BinaryConstraint.class);
		module.addDeserializer(Constraint.class, deserializer);
		return module;
	}
}