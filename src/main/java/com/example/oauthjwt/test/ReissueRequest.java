package com.example.oauthjwt.test;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ReissueRequest {

    private String refreshToken;
}
