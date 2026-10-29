package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User extends BaseEntity {
    public String name;
    public String username;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public String password;
    public String role;
    public Boolean enabled;
}
