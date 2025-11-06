package org.oneog.uppick.domain.notification.command.repository;

import java.util.List;

import org.oneog.uppick.domain.notification.command.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationJpaRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByMemberIdAndIsReadFalse(Long memberId);
}
