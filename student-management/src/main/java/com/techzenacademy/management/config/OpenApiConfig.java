package com.techzenacademy.management.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OpenApiConfig {

    @Value("${openapi.title}")
    String title;
    @Value("${openapi.description}")
    String description;
    @Value("${openapi.version}")
    String version;

    @Value("${openapi.servers[0].url}")
    String server0Url;
    @Value("${openapi.servers[0].description}")
    String server0Desc;

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info()
                        .title(title)
                        .description(description)
                        .version(version))
                .servers(List.of(
                        new Server().url(server0Url).description(server0Desc)
                ));
    }

    @Bean
    public GroupedOpenApi usersGroup() {
        return GroupedOpenApi.builder()
                .group("users")
                .packagesToScan("com.techzenacademy.management.controller")
                .build();
    }
}
