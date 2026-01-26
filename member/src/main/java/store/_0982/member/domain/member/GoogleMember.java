package store._0982.member.domain.member;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "google_member", schema = "member_schema")
@Getter
public class GoogleMember {
    @Id
    private String email;
    @Column
    private String name;
    @Column
    private String sub;

    public static GoogleMember createGoogleMember(String email, String name, String sub) {
        GoogleMember googleMember = new GoogleMember();
        googleMember.email = email;
        googleMember.name = name;
        googleMember.sub = sub;
        return googleMember;
    }
}
