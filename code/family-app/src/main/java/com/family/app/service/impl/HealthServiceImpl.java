package com.family.app.service.impl;

import com.family.app.service.HealthService;
import org.springframework.stereotype.Service;

@Service
public class HealthServiceImpl implements HealthService {

    @Override
    public String getStatus() {
        return "OK";
    }
}
