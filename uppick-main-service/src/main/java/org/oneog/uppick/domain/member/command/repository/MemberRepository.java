package org.oneog.uppick.domain.member.command.repository;

import java.util.Optional;

import org.oneog.uppick.domain.member.command.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

	boolean existsByEmail(String email);

	boolean existsByNickname(String nickname);

	Optional<Member> findByEmail(String email);

}
