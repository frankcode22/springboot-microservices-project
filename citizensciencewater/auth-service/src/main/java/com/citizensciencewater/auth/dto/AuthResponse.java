package com.citizensciencewater.auth.dto;

//==================== Auth Response ====================
public class AuthResponse {
private String token;
private String type = "Bearer";
private String username;
private String email;
private String role;
private String citizenId; // NEW FIELD: To return the unique citizen identifier

public AuthResponse(String token, String username, String email, String role) {
   this.token = token;
   this.username = username;
   this.email = email;
   this.role = role;
}

// NEW CONSTRUCTOR: Now accepts citizenId
public AuthResponse(String token, String username, String email, String role, String citizenId) {
   this.token = token;
   this.username = username;
   this.email = email;
   this.role = role;
   this.citizenId = citizenId;
}

public String getToken() {
   return token;
}

public void setToken(String token) {
   this.token = token;
}

public String getType() {
   return type;
}

public void setType(String type) {
   this.type = type;
}

public String getUsername() {
   return username;
}

public void setUsername(String username) {
   this.username = username;
}

public String getEmail() {
   return email;
}

public void setEmail(String email) {
   this.email = email;
}

public String getRole() {
   return role;
}

public void setRole(String role) {
   this.role = role;
}

// NEW GETTER
public String getCitizenId() {
   return citizenId;
}

// NEW SETTER
public void setCitizenId(String citizenId) {
   this.citizenId = citizenId;
}
}
