package store._0982.member.application.member;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.exception.CustomException;
import store._0982.member.application.member.dto.MemberSignUpCommand;
import store._0982.member.application.member.dto.MemberSignUpInfo;
import store._0982.member.domain.member.*;
import store._0982.member.exception.CustomErrorCode;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final GoogleMemberRepository googleMemberRepository;
    private final MemberRepository memberRepository;
    private final MemberFacade memberFacade;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        // 1. 구글이 내려준 유저 정보 받아오기
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 2. 구글 표준 클레임
        String email = (String) attributes.get("email");
        String name  = (String) attributes.getOrDefault("name", email);
        String sub   = (String) attributes.get("sub"); // 구글 고유 ID

        // 3. DB 조회
        GoogleMember googleMember = googleMemberRepository.findByEmail(email).orElse(null);
        if(googleMember == null){
            googleMember = GoogleMember.createGoogleMember(email,name,sub);
            googleMemberRepository.save(googleMember);
        }

        Member member = memberRepository.findByEmail(email).orElse(null);
        if(member == null){
            MemberSignUpInfo memberSignUpInfo = memberFacade.createGoogleMember(new MemberSignUpCommand(email, UUID.randomUUID().toString(), name, "000-0000-0000"));
            //todo: 이후 별도의 회원 정보 수정이 가입 페이지 필요, 비밀번호 찾기
            member = memberRepository.findById(memberSignUpInfo.memberId()).orElseThrow(()->new CustomException(CustomErrorCode.NOT_EXIST_MEMBER));
        }

        // 5. 이후 시큐리티 컨텍스트에 올라갈 principal 생성
        return new CustomOAuth2User(
                member.getMemberId(),
                member.getEmail(),
                member.getName(),
                member.getRole().name(),
                attributes
        );
    }
}
