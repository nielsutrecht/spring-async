package com.nibado.example.springasync.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AggregateResponse {
    private List<ApiResponse> responses;
    private int duration;
}
