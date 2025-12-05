package store._0982.member.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class OpenApiConfig {
    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Value("${gateway.host:localhost:8000}")
    private String host;
    @Value("${spring.application.name}")
    private String appName;

    @Bean
    public OpenAPI openAPI(){
        Server server = new Server();
        // Swagger Servers 항목을 게이트웨이 주소 하나로 고정
        server.url(host);// server.url(host+"/api/"+appName);
        List<Server> serverList = new ArrayList<>();
        serverList.add(server);

        SecurityScheme scheme = new SecurityScheme()
                .name("accessToken")
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.COOKIE);

        return new OpenAPI()
                .info(new Info().title("Member Service API").version("v1"))
                .servers(serverList)
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME, scheme))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}
