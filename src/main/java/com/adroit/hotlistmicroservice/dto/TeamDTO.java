package com.adroit.hotlistmicroservice.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TeamDTO {
    private String teamLeadId;
    private String teamLeadName;
    private List<AssociatedUser> recruiters = new ArrayList<>();
    private List<AssociatedUser> employees = new ArrayList<>();
    private List<AssociatedUser> salesExecutives = new ArrayList<>();
    private List<AssociatedUser> coordinators = new ArrayList<>();
}
