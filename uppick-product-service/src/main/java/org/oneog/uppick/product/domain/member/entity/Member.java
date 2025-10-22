package org.oneog.uppick.product.domain.member.entity;

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

	@Column(name = "email", nullable = false, unique = true)
	private String email;

	@Column(name = "nickname", nullable = false, unique = true)
	private String nickname;

	@Column(name = "password", nullable = false)
	private String password;

	@Column(nullable = false)
	private Long credit = 0L;

	@CreatedDate // 엔티티가 생성되어 저장될 때 자동으로 현재 시간 기록
	@Column(name = "registered_at", nullable = false)
	private LocalDateTime registeredAt;
}
