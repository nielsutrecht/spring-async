package com.nibado.example.springasync.domain;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {
    private String body;
    private int status;
    private int duration;

    @JsonRawValue
    public String getBody() {
        return body;
    }
}
