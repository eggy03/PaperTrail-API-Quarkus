package io.github.eggy03.exceptions.entity;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@RegisterForReflection
public class ErrorResponse {

    private int status;

    private String error;

    private String message;

    private LocalDateTime timeStamp;

    private String path;
}
