package com.adroit.hotlistmicroservice.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

        private String userId;
        private String userName;
        private String password;
        private String confirmPassword;
        private String email;
        private String personalemail;
        private String phoneNumber;
        private String dob;
        private String gender;
        private LocalDate joiningDate;
        private String designation;
        private Set<String> roles;
       private String status;
       private String entity;
       private String teamName;
       private List<TeamAssignment> teamAssignments;
       private Boolean isPrimarySuperAdmin;

}