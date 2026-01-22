package com.ecommerce.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "addresses")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;

    @NotBlank
    @Size(min = 5, message = "Steet name must be atleast 5 character")
    private String street;

    @NotBlank
    @Size(min = 5, message = "Building name must be atleast 5 character")
    private String buildingName;

    @NotBlank
    @Size(min = 2, message = "Address name must be atleast 2 character")
    private String city;

    @NotBlank
    @Size(min = 4, message = "State name must be atleast 4 character")
    private String state;

    @NotBlank
    @Size(min = 2, message = "Country name must be atleast 2 character")
    private String country;

    @NotBlank
    @Size(min = 6, message = "Pincode name must be atleast 6 character")
    private String pincode;

    @ManyToMany(mappedBy = "addresses")
    private List<User> users = new ArrayList<>();

}
