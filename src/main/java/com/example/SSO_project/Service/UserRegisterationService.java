package com.example.SSO_project.Service;

import com.example.SSO_project.domain.RegisterRequest;

import com.example.SSO_project.domain.UserRegister;



public interface UserRegisterationService {
    UserRegister registerUser(RegisterRequest request);
}