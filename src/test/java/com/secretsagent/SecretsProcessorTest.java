package com.secretsagent;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

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

public class SecretsProcessorTest
{
    @Mock
    SecretsManager secretsManager;

    Properties properties;

    @BeforeMethod
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);

        properties = new Properties();
        properties.setProperty("secret.name", "test-secret");

        String directoryPath = Paths.get(System.getProperty("user.dir"), "/local").toString();
        properties.setProperty("directory", directoryPath);
        properties.setProperty("secrets.filename", "/secrets/secrets.properties");
    }

    @AfterMethod
    public void tearDown()
    {
        // report Mockito problems in the test where they occur (default is in the next run)
        Mockito.validateMockitoUsage();

        // this may be needed to reset the mock
        Mockito.reset(secretsManager);
    }

    @Test
    public void testProcessSecret()
    {
        String secretString = "service.status.encryptedPassword=XXXXXXXXXXX==\nauth.accountcache.superUserPasswordEncrypted=XXXXXXXXXXX==\nauth" +
            ".accountcache.superUserName=service/user.test@testdomain.com\ndomainregistry.testdomain.auth" +
            ".passwordEncrypted=XXXXX==\ndomainregistry.testdomain.auth.userName=service/user.test@testdomain.com";

        List<String> expectedResult = Arrays.asList(secretString.split("\\\\n"));

        when(secretsManager.getSecret(properties.getProperty("secret.name"))).thenReturn(secretString);

        // test
        SecretsProcessor secretsProcessor = new SecretsProcessor(secretsManager, properties);
        List<String> actualResult =  secretsProcessor.processSecret();

        // verify
        Assert.assertEquals(actualResult, expectedResult);

        // verify getSecretValue is called only once and with the expected values
        Mockito.verify(secretsManager).getSecret(anyString());
        Mockito.verify(secretsManager).getSecret(properties.getProperty("secret.name"));
    }
}