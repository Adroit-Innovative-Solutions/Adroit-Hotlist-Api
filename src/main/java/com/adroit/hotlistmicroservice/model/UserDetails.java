package com.adroit.hotlistmicroservice.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class UserDetails {

    @Id
    private String userId;
    private String userName;

}



