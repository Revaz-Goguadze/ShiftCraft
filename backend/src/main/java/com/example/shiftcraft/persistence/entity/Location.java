package com.example.shiftcraft.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "locations")
public class Location {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @NotBlank
    @Column(nullable = false)
    private String name;
    
    @Column
    private String timezone;
    
    @Column(name = "address_line")
    private String addressLine;
    
    @Column
    private String city;
    
    @Column
    private String state;
    
    @Column(name = "postal_code")
    private String postalCode;
    
    @Column
    private String country;
    
    // One-to-many relationship with ShiftTemplate
    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ShiftTemplate> shiftTemplates = new HashSet<>();
    
    // Constructors
    public Location() {}
    
    public Location(String name, String timezone) {
        this.name = name;
        this.timezone = timezone;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    
    public String getAddressLine() { return addressLine; }
    public void setAddressLine(String addressLine) { this.addressLine = addressLine; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    
    public Set<ShiftTemplate> getShiftTemplates() { return shiftTemplates; }
    public void setShiftTemplates(Set<ShiftTemplate> shiftTemplates) { this.shiftTemplates = shiftTemplates; }
    
    @Override
    public String toString() {
        return "Location{name='" + name + "', city='" + city + "'}";
    }
}