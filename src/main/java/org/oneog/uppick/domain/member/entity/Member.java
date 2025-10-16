package org.oneog.uppick.domain.member.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
@EntityListeners(AuditingEntityListener.class)
public class Member {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "email", nullable = false)
	private String email;

	@Column(name = "nickname", nullable = false)
	private String nickname;

	@Column(name = "password", nullable = false)
	private String password;

	@Column(name = "name")
	private String name;

	@Column(name = "address")
	private String address;

	@Column(name = "phone")
	private String phone;

	@Column(nullable = false)
	private Long credit = 0L;

	@CreatedDate// 엔티티가 생성되어 저장될 때 자동으로 현재 시간 기록
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
