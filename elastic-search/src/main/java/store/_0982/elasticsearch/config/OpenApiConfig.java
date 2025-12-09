package store._0982.elasticsearch.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
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

    @Value("${gateway.host:localhost:8000}")
    private String host;

    @Bean
    public OpenAPI openAPI(){
        Server server = new Server();
        server.url(host);

        List<Server> serverList = new ArrayList<>();
        serverList.add(server);

        return new OpenAPI()
                .info(new Info().title("elastic-search" + " API").version("v1"))
                .servers(serverList);
    }
}
