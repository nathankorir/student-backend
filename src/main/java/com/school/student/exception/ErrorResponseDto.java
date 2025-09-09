package com.school.student.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDto {
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    private String path;
    @Builder.Default
    private Integer errorCode = 200;
    private String detail;
}
