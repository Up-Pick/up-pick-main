package org.oneog.uppick.domain.member.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oneog.uppick.common.dto.AuthMember;
import org.oneog.uppick.common.exception.BusinessException;
import org.oneog.uppick.domain.member.dto.request.CreditChargeRequest;
import org.oneog.uppick.domain.member.dto.response.CreditChargeResponse;
import org.oneog.uppick.domain.member.entity.Member;
import org.oneog.uppick.domain.member.exception.MemberErrorCode;
import org.oneog.uppick.domain.member.repository.MemberRepository;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {
	@Mock
	private MemberRepository memberRepository;

	@InjectMocks
	private MemberInternalService memberInternalService;

	private AuthMember authMember;
	private Member testMember;

	@BeforeEach
	void setUp() {

		authMember = new AuthMember(1L, "testUser");

		testMember = Member.builder()
			.email("test@email.com")
			.nickname("testUser")
			.password("encodedPass")
			.credit(0L)
			.build();

		// Member 엔티티의 ID는 @Id @GeneratedValue라 private여서 ReflectionTestUtils로 ID를 강제 주입
		ReflectionTestUtils.setField(testMember, "id", 1L);
	}

	@Test
	@DisplayName("chargeCredit_정상적인요청_성공")
	void chargeCredit_정상적인요청_성공() {
		// given
		CreditChargeRequest request = new CreditChargeRequest(10000L); // 10,000원 충전 요청
		// memberId(1L)로 조회 시 testMember 객체를 반환
		when(memberRepository.findById(authMember.getMemberId())).thenReturn(Optional.of(testMember));

		// when
		CreditChargeResponse response = memberInternalService.chargeCredit(request, authMember);

		// then
		assertNotNull(response);
		// testMember의 초기 credit 0L + 10000L = 10000L
		assertEquals(10000L, response.getCurrentCredit());
		// member 객체의 credit 값이 실제로 10000L이 되었는지 확인
		assertEquals(10000L, testMember.getCredit());
		// memberRepository.findById가 1번 호출되었는지 검증
		verify(memberRepository, times(1)).findById(1L);
	}

	@Test
	@DisplayName("chargeCredit_존재하지않는사용자_BusinessException발생")
	void chargeCredit_존재하지않는사용자_BusinessException발생() {
		// given
		CreditChargeRequest request = new CreditChargeRequest(10000L);
		// memberId(1L)로 조회 시 빈 Optional을 반환
		when(memberRepository.findById(anyLong())).thenReturn(Optional.empty());

		// when & then
		// memberInternalService.chargeCredit 실행 시 BusinessException이 발생하는지 검증
		BusinessException exception = assertThrows(BusinessException.class, () -> {
			memberInternalService.chargeCredit(request, authMember);
		});

		// 발생한 예외의 ErrorCode가 MEMBER_NOT_FOUND인지 확인
		assertEquals(MemberErrorCode.MEMBER_NOT_FOUND, exception.getErrorCode());
	}

	@Test
	@DisplayName("chargeCredit_0원금액충전_BusinessException발생")
	void chargeCredit_0원금액충전_BusinessException발생() {
		// given
		CreditChargeRequest request = new CreditChargeRequest(0L); // 0원 충전 요청
		// 사용자는 정상적으로 조회됨
		when(memberRepository.findById(authMember.getMemberId())).thenReturn(Optional.of(testMember));

		// when & then
		// Member 엔티티의 addCredit 메소드에서 예외가 발생해야 함
		BusinessException exception = assertThrows(BusinessException.class, () -> {
			memberInternalService.chargeCredit(request, authMember);
		});

		// 발생한 예외의 ErrorCode가 INVALID_CHARGE_AMOUNT인지 확인
		assertEquals(MemberErrorCode.INVALID_CHARGE_AMOUNT, exception.getErrorCode());
		// 0원 충전 시도 시, member의 크레딧은 0L 그대로여야 함
		assertEquals(0L, testMember.getCredit());
	}
}
