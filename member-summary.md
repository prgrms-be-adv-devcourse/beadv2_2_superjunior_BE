# member 서비스 주요 기능 요약

## 1. 회원가입 및 프로필 관리

- 주요 엔드포인트 (컨트롤러)
  - 회원가입: `POST /api/members` — `MemberController.createMember`
  - 회원 탈퇴: `DELETE /api/members` — `MemberController.deleteMember`
  - 비밀번호 변경: `PUT /api/members/password` — `MemberController.changePassword`
  - 프로필 조회/수정: `GET/PUT /api/members/profile` — `MemberController.getProfile`, `MemberController.updateProfile`

- 핵심 서비스 로직 (`MemberService`)
  - `createMember(MemberSignUpCommand)`  
    - 이메일/이름 중복 체크, 이메일 인증 여부 확인 후 회원 생성  
    - `Member.create(...)`로 엔티티 생성 후 `saltKey + password` 를 BCrypt로 암호화
  - `changePassword(PasswordChangeCommand)`  
    - 회원 조회 → `checkPassword` 로 현재 비밀번호 검증 → 새 비밀번호 암호화 후 저장
  - `deleteMember(MemberDeleteCommand)`  
    - 비밀번호 검증 후 soft delete (`deletedAt` 설정 후 저장)
  - `updateProfile(ProfileUpdateCommand)` / `getProfile(UUID memberId)`  
    - 이름 중복 체크 후 이름·전화번호 수정, 혹은 조회

- 관련 도메인 (`Member`)
  - 주요 필드: `memberId`, `email`, `name`, `password`, `phoneNumber`, `role`, `saltKey`, `createdAt`, `updatedAt`, `deletedAt`
  - 주요 메서드:  
    - `create(String email, String name, String password, String phoneNumber)`  
    - `encodePassword(String password)`, `changePassword(String password)`  
    - `update(String name, String phoneNumber)`  
    - `delete()`, `registerSeller()`


## 2. 로그인/인증 및 JWT 토큰 발급

- 주요 엔드포인트 (`AuthController`)
  - 로그인: `POST /api/auth/login`  
    - 이메일/비밀번호로 로그인, accessToken/refreshToken 쿠키 발급
  - 액세스 토큰 재발급: `GET /api/auth/refresh`  
    - `refreshToken` 쿠키 기반 accessToken 재발급
  - 로그아웃: `GET /api/auth/logout`  
    - accessToken/refreshToken 쿠키 모두 만료 처리

- 핵심 서비스 로직 (`AuthService`)
  - `login(MemberLoginCommand)`  
    - 이메일로 회원 조회 → `checkPassword` 로 비밀번호 검증  
    - `JwtProvider.generateAccessToken`, `generateRefreshToken` 호출 후 `LoginTokens` 반환
  - `refreshAccessToken(String refreshToken)`  
    - 토큰 null 체크 및 파싱 → memberId 로 회원 조회 → 새 accessToken 발급
  - `logout(Cookie[] cookies)`  
    - 이름에 `Token` 이 포함된 쿠키들을 찾아 `setMaxAge(0)` 으로 만료

- JWT 발급/검증 (`JwtProvider`)
  - 생성자에서 `secretKey`, access/refresh 만료 시간(ms) 주입
  - `generateAccessToken(Member member)` / `generateRefreshToken(Member member)`  
    - `subject` 에 `memberId`, `claim` 에 `email`, `role` 저장  
    - `issuer` 는 `"member-service"`, HS256 서명
  - `getMemberIdFromToken(String token)`  
    - 토큰 파싱 후 `subject` 를 `UUID` 로 변환하여 반환


## 3. 이메일 인증 (토큰 발급/검증 및 배치 삭제)

- 주요 엔드포인트 (`MemberController`)
  - 이메일 인증 메일 전송: `GET /api/members/email/{email}` — `sendVerificationEmail`  
  - 이메일 인증 완료: `GET /api/members/email/verification/{token}` — `verifyEmail`

- 핵심 서비스 로직 (`MemberService`)
  - `sendVerificationEmail(String email)`  
    - 이메일 중복 체크 (`checkEmailDuplication`)  
    - `EmailTokenRepository.findByEmail(email)` 결과가 있으면 `EmailToken.refresh()`, 없으면 `EmailToken.create(email)`  
    - 저장 후 `EmailService.sendEmail` 로 인증 코드 메일 전송
  - `verifyEmail(String token)`  
    - `EmailTokenRepository.findByToken(token)` 으로 조회  
    - 만료 여부(`isExpired`) 확인 후 검증 완료(`verify`) 처리
  - `checkEmailVerification(String email)`  
    - 회원가입 시 해당 이메일의 `EmailToken.isVerified()` 가 true 인지 확인

- 이메일 토큰 도메인 (`EmailToken`)
  - 테이블: `email_token` (schema: `member_schema`)
  - 주요 필드: `emailTokenId`, `email`, `token`, `isVerified`, `expiredAt`, `createdAt`, `updatedAt`
  - 유효 시간: `VALID_MINUTES = 3`
  - 주요 메서드:  
    - `create(String email)` — 새 토큰 생성 및 3분 유효기간 설정  
    - `refresh()` — 토큰 재발급 및 유효기간 연장  
    - `isExpired()` — 현재 시간이 `expiredAt` 초과인지 검사  
    - `verify()` — `isVerified` 를 true 로 변경

- 배치 삭제 (`DeleteEmailTokenScheduler`, `EmailTokenService`)
  - `DeleteEmailTokenScheduler.cleanupExpiredTokens()`  
    - 크론: `"0 0 3 * * *"` (매일 새벽 3시)  
    - `emailTokenService.cleanUpExpiredEmailTokens()` 호출
  - `EmailTokenService.cleanUpExpiredEmailTokens()`  
    - `emailTokenRepository.deleteExpiredEmailTokens(OffsetDateTime.now())` 호출
  - 인프라: `EmailTokenRepositoryAdpater` → `EmailTokenJpaRepository.deleteExpiredTokens`  
    - JPA 쿼리: `delete from EmailToken et where et.expiredAt < :now`


## 4. 주소 관리

- 주요 엔드포인트 (`MemberController`)
  - 주소 등록: `POST /api/members/address` — `addAddress`
  - 주소 목록 조회: `GET /api/members/addresses` — `getAddresses`
  - 주소 삭제: `DELETE /api/members/address/{addressId}` — `deleteAddress` (메서드명은 `addAddress`)

- 핵심 서비스 로직 (`MemberService`)
  - `addAddress(AddressAddCommand)`  
    - `memberId` 로 회원 조회 → `Address.create(member, ...)` 로 엔티티 생성 후 저장
  - `getAddresses(Pageable pageable, UUID memberId)`  
    - `AddressRepository.findAllByMemberId` 결과를 `Page<AddressInfo>` 로 매핑  
    - 공통 `PageResponse.from` 으로 감싸서 반환
  - `deleteAddress(AddressDeleteCommand)`  
    - 주소 조회 → `checkAddressOwner(address, memberId)` 로 소유자 검증 → 삭제

- 주소 도메인 (`Address`)
  - 테이블: `address` (schema: `member_schema`)
  - `ManyToOne` 관계로 `Member` 에 연결, `memberId` 필드는 조회용
  - 생성 메서드: `create(Member member, String address, String addressDetail, String postalCode, String receiverName, String phoneNumber)`


## 5. 판매자 등록 및 계좌 관리

- 주요 엔드포인트 (`MemberController`, `InternalController`)
  - 판매자 등록: `POST /api/members/seller` — `registerSeller` (CONSUMER 권한 필요)  
  - 판매자 정보 조회: `GET /api/members/seller/{sellerId}` — `getSeller`  
  - 판매자 정보 수정: `PUT /api/members/seller` — `updateSeller` (SELLER 권한 필요)  
  - 내부용 판매자 계좌 조회: `POST /internal/members/seller-account` — `InternalController.getSellerAccountInfo`

- 핵심 서비스 로직 (`SellerService`)
  - `registerSeller(SellerRegisterCommand)`  
    - 회원 조회 후 `Seller.create(member, ...)` 로 판매자 엔티티 생성  
    - 생성 시 `member.registerSeller()` 호출로 Role 을 SELLER 로 변경
  - `getSeller(SellerCommand)`  
    - 판매자 조회 후 `SellerInfo` 로 변환  
    - 요청자가 본인이 아닐 경우 `sellerInfo.blind()` 로 민감 정보 마스킹
  - `updateSeller(SellerRegisterCommand)`  
    - 판매자 조회 후 계좌/사업자 정보 수정
  - `getSellerAccountList(SellerAccountListCommand)` (내부용)  
    - sellerIds 리스트로 조회 후 `SellerAccountInfo` 리스트 반환

- 판매자 도메인 (`Seller`)
  - `@OneToOne` + `@MapsId` 구조로 `Member` 와 동일 PK 공유 (seller_id == member_id)
  - 은행 코드, 계좌 번호, 예금주, 사업자등록번호 등을 보유  
  - 주요 메서드:  
    - `create(Member member, String bankCode, String accountNumber, String accountHolder, String businessRegistrationNumber)`  
    - `update(String bankCode, String accountNumber, String accountHolder, String businessRegistrationNumber)`


## 6. 내부(internal) API

- 컨트롤러: `InternalController` (`/internal/members`, `@Hidden`)
  - 판매자 계좌 조회 (내부용): `POST /internal/members/seller-account`  
    - `SellerAccountListRequest.toCommand()` → `SellerService.getSellerAccountList` 호출
  - 프로필 조회 (내부용): `GET /internal/members/profile`  
    - `HeaderName.ID` 헤더로 memberId 를 받아 `MemberService.getProfile` 호출

- 특징
  - Swagger 문서에서 숨기기 위해 `@Hidden` 사용  
  - 외부 클라이언트가 아닌 게이트웨이/내부 서비스 간 통신용 엔드포인트


## 7. 보안 및 인프라 설정 주요 포인트

- 비밀번호 인코딩 (`SecurityConfig`)
  - `BCryptPasswordEncoder(11)` 를 빈으로 등록
  - 실제 저장 시 `member.saltKey + rawPassword` 에 대해 BCrypt 적용

- JWT 설정 (`application.yml`, `JwtProvider`)
  - `jwt.secret`, `jwt.access-token-validity-period`, `jwt.refresh-token-validity-period` 환경 변수로 관리
  - 토큰에는 `memberId`, `email`, `role` 클레임을 포함

- 이메일 발송 (`EmailService`, `SmtpEmailServiceAdapter`)
  - `EmailService.sendEmail(String from, String to, String subject, String text)` 인터페이스
  - `SmtpEmailServiceAdapter` 가 `JavaMailSender` 를 사용하여 실제 SMTP 메일 발송

