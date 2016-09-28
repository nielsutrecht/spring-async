package com.nibado.example.springasync.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ApiRequest {
    private List<String> urls;
}
