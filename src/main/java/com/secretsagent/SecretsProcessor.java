package com.secretsagent;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Copyright 2020 Comcast Cable Communications Management, LLC
 *
 * Licensed under the Apache License, Version2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

public class SecretsProcessor
{
    public static final String SERVICE_PROPERTIES = "/config/service.properties";
    public static final String CONTAINER_DIRECTORY = "/app";
    public static final String LOCAL_DIRECTORY = "/local";

    private static final Logger logger = LogManager.getLogger(SecretsProcessor.class);

    SecretsManager secretsManager;
    Properties properties;

    public SecretsProcessor(SecretsManager secretsManager, Properties properties)
    {
        this.secretsManager = secretsManager;
        this.properties = properties;
    }

    public SecretsProcessor()
    {
        this.properties = getProperties();

        String region = properties.getProperty("region");
        boolean isLocalRun = false;
        if (StringUtils.isNotEmpty(properties.getProperty("is.local.run")))
        {
            isLocalRun = Boolean.parseBoolean(properties.getProperty("is.local.run"));
        }

        this.secretsManager = new SecretsManager(isLocalRun, region);
    }

    public List<String> processSecret()
    {
        // *** get secret
        String secretName = properties.getProperty("secret.name");
        String secret = secretsManager.getSecret(secretName);

        // *** write file
        List<String> lines = Arrays.asList(secret.split("\\\\n"));

        Path secretsPath = Paths.get(properties.getProperty("directory"), properties.getProperty("secrets.filename"));

        try
        {
            writeFile(lines, secretsPath);
        }
        catch (IOException ex)
        {
            logger.error("Error writing to file", ex);
        }

        logger.info("Completed successfully.");

        // return lines for testing
        return lines;
    }

    private Properties getProperties()
    {
        Properties properties = new Properties();

        Path directoryPath = Paths.get(CONTAINER_DIRECTORY);

        if (Files.notExists(directoryPath))
        {
            directoryPath = Paths.get(System.getProperty("user.dir"), LOCAL_DIRECTORY);
        }
        String directory = directoryPath.toString();

        Path propertiesPath = Paths.get(directoryPath.toString(), SERVICE_PROPERTIES);

        try
        {
            properties.load(new FileReader(String.valueOf(propertiesPath)));
        }
        catch (IOException e)
        {
            logger.warn("Unable to load properties.");
        }

        properties.setProperty("directory", directory);

        return properties;
    }

    private void writeFile(List<String> lines, Path path) throws IOException
    {
        logger.info("Attempting to write to: {}", path);

        // create the parent directories if they don't exist
        File file = new File(path.toString());
        file.getParentFile().mkdirs();

        Files.write(path, lines, StandardCharsets.UTF_8);
    }
}
