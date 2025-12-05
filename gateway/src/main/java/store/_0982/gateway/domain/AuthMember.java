package store._0982.gateway.domain;

/**
 * 게이트웨이를 통과하는 인증된 회원 정보를 표현하는 도메인 모델.
 * (필요 시 필드/메서드를 확장해서 재사용할 수 있도록 분리)
 */
public class AuthMember {

    private final String id;
    private final String email;
    private final String role;

    public AuthMember(String id, String email, String role) {
        this.id = id;
        this.email = email;
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}

