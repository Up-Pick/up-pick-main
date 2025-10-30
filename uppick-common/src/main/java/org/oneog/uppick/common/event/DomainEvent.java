package org.oneog.uppick.common.event;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public abstract class DomainEvent {

    private final String eventId;
    private final LocalDateTime occurredAt;

}
