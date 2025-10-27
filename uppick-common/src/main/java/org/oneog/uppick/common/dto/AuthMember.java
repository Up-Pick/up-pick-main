package org.oneog.uppick.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthMember {
    private long memberId;
    private String memberNickname;
}
