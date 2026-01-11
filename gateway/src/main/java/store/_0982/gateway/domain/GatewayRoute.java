package store._0982.gateway.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "gateway_route", schema = "gateway_schema")
public class GatewayRoute {

    @Id
    @Column("id")
    private UUID id;

    @Column("http_method")
    private String httpMethod;

    @Column("endpoint")
    private String endpoint;

    @Column("roles")
    private String roles;

    // DB에서 id 를 gen_random_uuid() 로 생성하게 둘 때 쓰는 생성자
    public GatewayRoute(String httpMethod, String endpoint, String roles) {
        this.httpMethod = httpMethod;
        this.endpoint = endpoint;
        this.roles = roles;
    }
}
