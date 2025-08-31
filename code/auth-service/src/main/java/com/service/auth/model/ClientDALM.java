package com.service.auth.model;


import java.util.Date;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ClientDALM {
    protected UUID uid;
    protected Date birthDate;
    protected String email;
    protected String password;
    protected String username;
}

