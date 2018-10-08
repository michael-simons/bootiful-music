package ac.simons.music.knowledge.config;

import java.util.Optional;

import org.neo4j.ogm.driver.ParameterConversionMode;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Neo4jConfiguration {
	@Bean
	public org.neo4j.ogm.config.Configuration configuration(Neo4jProperties properties) {
		var builder = new org.neo4j.ogm.config.Configuration.Builder();
		builder.uri(Optional.ofNullable(properties.getUri()).orElse("bolt://localhost:7687"));
		builder.credentials(properties.getUsername(), properties.getPassword());
		builder.autoIndex(properties.getAutoIndex().getName());
		builder.withCustomProperty(ParameterConversionMode.CONFIG_KEY, ParameterConversionMode.CONVERT_NON_NATIVE_ONLY);
		return builder.build();
	}
}
