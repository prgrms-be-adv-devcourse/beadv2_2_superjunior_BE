package store._0982.batch.infrastructure.seller;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.stereotype.Repository;
import store._0982.batch.batch.sellerpayout.dto.SellerAccountDto;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Repository
public class SellerAccountJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public Map<UUID, SellerAccountDto> findAccountsBySellerIds(List<UUID> sellerIds) {
        String sql = """
              SELECT seller_id, bank_code, account_number, account_holder
              FROM member_schema.seller
              WHERE seller_id = ANY(?)
              """;

        PreparedStatementCreator psc = (Connection con) -> {
            PreparedStatement ps = con.prepareStatement(sql);
            Array array = con.createArrayOf("uuid", sellerIds.toArray());
            ps.setArray(1, array);
            return ps;
        };

        return jdbcTemplate.query(psc, rs -> {
            Map<UUID, SellerAccountDto> map = new HashMap<>();
            while (rs.next()) {
                UUID sellerId = (UUID) rs.getObject("seller_id");
                String bankCode = rs.getString("bank_code");
                String accountNumber = rs.getString("account_number");
                String accountHolder = rs.getString("account_holder");
                map.put(sellerId, new SellerAccountDto(sellerId, bankCode, accountNumber, accountHolder));
            }
            return map;
        });
    }
}
