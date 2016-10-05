package com.nibado.example.springasync.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(NON_NULL)
public class ApiResponse {
    private String body;
    private int status;
    private int duration;

    @JsonRawValue
    public String getBody() {
        if(body != null && body.trim().isEmpty()) {
            return null;
        }
        return body;
    }
}
