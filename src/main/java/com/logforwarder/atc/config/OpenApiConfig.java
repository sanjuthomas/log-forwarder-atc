package com.logforwarder.atc.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI logForwarderAtcOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("log-forwarder-atc")
                        .description("Air Traffic Controller for log-forwarder agents")
                        .version("0.1.0")
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
