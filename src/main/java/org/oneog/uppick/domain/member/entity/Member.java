package org.oneog.uppick.domain.member.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String email;

	@Column(nullable = false)
	private String nickname;

	@Column(nullable = false)
	private String password;

	private String name;

	private String address;

	private String phone;

	@Column(nullable = false)
	private Long credit = 0L;

	@CreationTimestamp// 엔티티가 생성되어 저장될 때 자동으로 현재 시간 기록
	@Column(name = "registered_at", nullable = false)
	private LocalDateTime registeredAt;

	@Builder
	public Member(String email, String nickname, String password, String name, String address, String phone,
		Long credit) {

		this.email = email;
		this.nickname = nickname;
		this.password = password;
		this.name = name;
		this.address = address;
		this.phone = phone;
		this.credit = credit;

	}
}
