package com.processmonster.bpm.mapper;

import com.processmonster.bpm.dto.webhook.WebhookDTO;
import com.processmonster.bpm.dto.webhook.WebhookDeliveryDTO;
import com.processmonster.bpm.entity.Webhook;
import com.processmonster.bpm.entity.WebhookDelivery;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for Webhook entity and DTOs
 */
@Mapper(componentModel = "spring")
public interface WebhookMapper {

    @Mapping(target = "active", expression = "java(webhook.isActive())")
    WebhookDTO toDTO(Webhook webhook);

    @Mapping(target = "webhookId", source = "webhook.id")
    @Mapping(target = "webhookName", source = "webhook.name")
    WebhookDeliveryDTO toDeliveryDTO(WebhookDelivery webhookDelivery);
}
