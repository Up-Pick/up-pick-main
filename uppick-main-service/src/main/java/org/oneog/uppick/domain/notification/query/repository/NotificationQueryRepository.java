package org.oneog.uppick.domain.notification.query.repository;

import java.util.Optional;

import org.oneog.uppick.domain.notification.command.entity.QNotification;
import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NotificationQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public long countUnreadNotificationsByMemberId(Long memberId) {

        QNotification notification = QNotification.notification;

        return Optional.ofNullable(jpaQueryFactory
            .select(notification.count())
            .from(notification)
            .where(
                notification.memberId.eq(memberId)
                    .and(notification.isRead.eq(false))
            )
            .fetchOne()).orElse(0L);
    }

}
