package store._0982.point.point.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Getter
@Entity
@Table(name = "member_point", schema = "point_schema")
public class MemberPoint {

    @Id
    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "point_balance")
    private Integer pointBalance;
}