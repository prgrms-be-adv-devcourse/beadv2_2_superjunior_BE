package store._0982.member.application.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store._0982.member.application.member.dto.*;
import store._0982.member.domain.member.Member;
import store._0982.member.domain.member.MemberRepository;
import store._0982.member.domain.member.Seller;
import store._0982.member.domain.member.SellerRepository;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SellerServiceTest {

    @Mock
    private SellerRepository sellerRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private SellerService sellerService;

    @Test
    @DisplayName("판매자 등록에 성공한다")
    void registerSeller_success() {
        // given
        Member member = Member.create("test@example.com", "tester", "password", "010-1111-2222");
        UUID memberId = member.getMemberId();

        SellerRegisterCommand command = new SellerRegisterCommand(
                memberId,
                "1234567890",
                "001",
                "홍길동",
                "123-45-67890"
        );

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(sellerRepository.save(any(Seller.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        SellerRegisterInfo result = sellerService.registerSeller(command);

        // then
        assertThat(result.accountNumber()).isEqualTo("1234567890");
        assertThat(result.bankCode()).isEqualTo("001");
        assertThat(result.accountHolder()).isEqualTo("홍길동");
        assertThat(result.businessRegistrationNumber()).isEqualTo("123-45-67890");
        verify(memberRepository).findById(memberId);
        verify(sellerRepository).save(any(Seller.class));
    }

    @Test
    @DisplayName("자기 자신의 판매자 정보를 조회하면 정보가 마스킹되지 않는다")
    void getSeller_self_success() throws Exception {
        // given
        Member member = Member.create("test@example.com", "tester", "password", "010-1111-2222");
        UUID sellerId = member.getMemberId();

        Seller seller = Seller.create(member, "001", "1234567890", "홍길동", "123-45-67890");
        setField(seller, "sellerId", sellerId);

        SellerCommand command = new SellerCommand(sellerId, sellerId);

        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));

        // when
        SellerInfo result = sellerService.getSeller(command);

        // then
        assertThat(result.sellerId()).isEqualTo(sellerId);
        assertThat(result.accountNumber()).isEqualTo("1234567890");
        assertThat(result.bankCode()).isEqualTo("001");
        assertThat(result.accountHolder()).isEqualTo("홍길동");
        assertThat(result.phoneNumber()).isEqualTo(member.getPhoneNumber());
        verify(sellerRepository).findById(sellerId);
    }

    @Test
    @DisplayName("판매자 정보를 수정하면 계좌 정보가 변경된다")
    void updateSeller_success() throws Exception {
        // given
        Member member = Member.create("test@example.com", "tester", "password", "010-1111-2222");
        UUID sellerId = member.getMemberId();

        Seller seller = Seller.create(member, "001", "1234567890", "홍길동", "123-45-67890");
        setField(seller, "sellerId", sellerId);

        SellerRegisterCommand command = new SellerRegisterCommand(
                sellerId,
                "9876543210",
                "002",
                "이몽룡",
                "987-65-43210"
        );

        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));

        // when
        SellerRegisterInfo result = sellerService.updateSeller(command);

        // then
        assertThat(result.accountNumber()).isEqualTo("9876543210");
        assertThat(result.bankCode()).isEqualTo("002");
        assertThat(result.accountHolder()).isEqualTo("이몽룡");
        assertThat(result.businessRegistrationNumber()).isEqualTo("987-65-43210");
        verify(sellerRepository).findById(sellerId);
    }

    @Test
    @DisplayName("판매자 ID 목록으로 계좌 정보 목록을 조회한다")
    void getSellerAccountList_success() {
        // given
        Member member1 = Member.create("user1@example.com", "user1", "password", "010-1111-2222");
        Member member2 = Member.create("user2@example.com", "user2", "password", "010-3333-4444");

        Seller seller1 = Seller.create(member1, "001", "1111111111", "홍길동", "123-45-67890");
        Seller seller2 = Seller.create(member2, "002", "2222222222", "이몽룡", "987-65-43210");

        List<UUID> sellerIds = List.of(UUID.randomUUID(), UUID.randomUUID());
        SellerAccountListCommand command = new SellerAccountListCommand(sellerIds);

        when(sellerRepository.findAllById(sellerIds)).thenReturn(List.of(seller1, seller2));

        // when
        List<SellerAccountInfo> result = sellerService.getSellerAccountList(command);

        // then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(SellerAccountInfo::bankCode)
                .containsExactlyInAnyOrder("001", "002");
        verify(sellerRepository).findAllById(sellerIds);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}

