package com.team.multiversaltcg.modules.auth;

import lombok.Data;

@Data
public class AuthRequest {

    private String username;
    private String password;
}