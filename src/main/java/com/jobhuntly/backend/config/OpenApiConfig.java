package com.jobhuntly.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI jobhuntlyOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("JobHuntly API")
                        .version("v1")
                        .description("OpenAPI for JobHuntly"))
                // Cho phép hiển thị nút Authorize và gắn security cho toàn cục (tùy bạn)
                .addSecurityItem(new SecurityRequirement().addList("cookieAuth"))
                .components(new Components()
                        // Dùng JWT trong HttpOnly Cookie tên "AT"
                        .addSecuritySchemes("cookieAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.COOKIE)
                                        .name("AT"))
                        // (Tuỳ chọn) cho phép test qua header Authorization: Bearer <token>
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }

    // Nhóm các route /api/v1/**
    @Bean
    public GroupedOpenApi publicV1() {
        return GroupedOpenApi.builder()
                .group("v1")
                .pathsToMatch("/api/v1/**")
                .build();
    }

//    @Bean
//    public OpenApiCustomiser globalResponses() {
//        return openApi -> openApi.getPaths().forEach((p, item) -> {
//            item.readOperations().forEach(op -> {
//                ApiResponses responses = op.getResponses();
//                responses.addApiResponse("401", new ApiResponse().description("Unauthorized"));
//                responses.addApiResponse("403", new ApiResponse().description("Forbidden"));
//            });
//        });
//    }
}

