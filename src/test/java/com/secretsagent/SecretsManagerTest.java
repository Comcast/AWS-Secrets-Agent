package com.secretsagent;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
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

public class SecretsManagerTest
{
    @Mock
    AWSSecretsManager awsSecretsManagerClient;

    @BeforeMethod
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    @AfterMethod
    public void tearDown()
    {
        // report Mockito problems in the test where they occur (default is in the next run)
        Mockito.validateMockitoUsage();

        // this may be needed to reset the mock
        Mockito.reset(awsSecretsManagerClient);
    }

    @Test
    public void testGetSecret_success()
    {
        String testSecretName = "my-secret";
        String expectedSecret = "my-secret-value";

        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
            .withSecretId(testSecretName);

        GetSecretValueResult getSecretValueResult = new GetSecretValueResult()
            .withSecretString(expectedSecret);

        when(awsSecretsManagerClient.getSecretValue(getSecretValueRequest)).thenReturn(getSecretValueResult);

        // test
        SecretsManager secretsManager = new SecretsManager(awsSecretsManagerClient);
        String actualSecret = secretsManager.getSecret(testSecretName);

        // verify
        Assert.assertEquals(actualSecret, expectedSecret);

        // verify getSecretValue is called only once and with the expected values
        Mockito.verify(awsSecretsManagerClient).getSecretValue(any(GetSecretValueRequest.class));
        Mockito.verify(awsSecretsManagerClient).getSecretValue(getSecretValueRequest);
    }
}