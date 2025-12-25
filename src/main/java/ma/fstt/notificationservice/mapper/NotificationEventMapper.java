package ma.fstt.notificationservice.mapper;


import ma.fstt.notificationservice.dto.*;
import ma.fstt.notificationservice.entities.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface NotificationEventMapper {

    NotificationEvent toResponseDto(Notification notification);

    Notification toEntity(NotificationEvent dto);

    void updateEntityFromDto(NotificationEvent dto, @MappingTarget Notification notification);
}
