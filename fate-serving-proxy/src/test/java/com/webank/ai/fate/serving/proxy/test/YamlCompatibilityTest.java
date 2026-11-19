/*
 * Copyright 2019 The FATE Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.ai.fate.serving.proxy.test;

import com.webank.ai.fate.serving.common.utils.TransferUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ByteArrayResource;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class YamlCompatibilityTest {
    @TempDir
    Path directory;

    @Test
    void loadsProfileSpecificSpringBootConfiguration() throws IOException {
        Path yaml = writeYaml("server:\n  port: 8080\n"
                + "---\nspring:\n  config:\n    activate:\n      on-profile: smoke\n"
                + "server:\n  port: 8081\n");
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(EmptyConfiguration.class)
                .web(WebApplicationType.NONE)
                .logStartupInfo(false)
                .run("--spring.config.location=" + yaml.toUri(), "--spring.profiles.active=smoke",
                        "--spring.main.banner-mode=off")) {
            assertEquals("8081", context.getEnvironment().getProperty("server.port"));
        }
    }

    @Test
    void rejectsUnrecognizedConfigurationTag() {
        ByteArrayResource yaml = new ByteArrayResource(
                "value: !unknown sample\n".getBytes(StandardCharsets.UTF_8));
        assertThrows(YAMLException.class, () -> new YamlPropertySourceLoader().load("test", yaml));
    }

    @Test
    void convertsNestedYamlWithJackson() throws IOException {
        Path yaml = writeYaml("parent:\n  child: value\n  enabled: true\n");
        assertEquals(Arrays.asList("parent.child=value", "parent.enabled=true"),
                TransferUtils.yml2Properties(yaml.toString()));
    }

    private Path writeYaml(String content) throws IOException {
        return Files.write(directory.resolve("application.yml"),
                content.getBytes(StandardCharsets.UTF_8));
    }

    @Configuration(proxyBeanMethods = false)
    static class EmptyConfiguration {
    }
}
