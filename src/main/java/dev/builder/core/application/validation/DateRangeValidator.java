package dev.builder.core.application.validation;

import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.properties.MessageLocalizer;

import java.time.LocalDateTime;

@Bean
public class DateRangeValidator {

    private final MessageLocalizer messageLocalizer;

    public DateRangeValidator(MessageLocalizer messageLocalizer) {
        this.messageLocalizer = messageLocalizer;
    }

    public void validate(String startFieldName, String endFieldName, LocalDateTime start, LocalDateTime end) throws FieldViolationException{
        if(start == null){
            throw new RequiredFieldViolation(messageLocalizer, startFieldName);
        }
        if(end == null){
            throw new RequiredFieldViolation(messageLocalizer, endFieldName);
        }
        if(start.isAfter(end)){
            throw new DateAfterViolation(messageLocalizer, endFieldName, start);
        }
    }
}
