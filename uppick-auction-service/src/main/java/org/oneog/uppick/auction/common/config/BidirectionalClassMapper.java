package org.oneog.uppick.auction.common.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.DefaultClassMapper;

public class BidirectionalClassMapper extends DefaultClassMapper {

    private final Map<Class<?>, String> classToIdMap = new HashMap<>();

    @Override
    public void setIdClassMapping(Map<String, Class<?>> idClassMapping) {

        super.setIdClassMapping(idClassMapping);
        // 역방향 매핑 생성: 클래스 -> ID
        idClassMapping.forEach((id, clazz) -> classToIdMap.put(clazz, id));

    }

    @Override
    public void fromClass(Class<?> clazz, MessageProperties properties) {

        // 먼저 매핑된 ID가 있는지 확인
        String typeId = classToIdMap.get(clazz);
        if (typeId != null) {
            properties.getHeaders().put("__TypeId__", typeId);
        } else {
            // 없으면 기본 동작 (풀 클래스명)
            super.fromClass(clazz, properties);
        }
    }

}
