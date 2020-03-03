package com.webank.ai.fate.serving;

import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.IOException;

public class MyPropertySourceFactory implements PropertySourceFactory {
    public MyPropertySourceFactory() {
    }

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {


        return name != null ? new ResourcePropertySource(name, resource) : new ResourcePropertySource(resource);
    }
}