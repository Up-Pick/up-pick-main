package org.oneog.uppick.domain.notification.repository;

import org.oneog.uppick.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationJpaRepository extends JpaRepository<Notification, Long> {

}
