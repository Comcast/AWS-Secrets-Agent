package com.secretsagent;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfilesConfigFile;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

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


public class SecretsManager
{
    private static final Logger logger = LogManager.getLogger(SecretsManager.class);

    private AWSSecretsManager awsSecretsManagerClient;

    public SecretsManager(AWSSecretsManager awsSecretsManagerClient)
    {
        this.awsSecretsManagerClient = awsSecretsManagerClient;
    }

    public SecretsManager(boolean isLocalRun, String region)
    {
        AWSCredentialsProvider credentials = getAwsCredentialsProvider(isLocalRun);

        this.awsSecretsManagerClient = AWSSecretsManagerClientBuilder.standard()
            .withCredentials(credentials)
            .withRegion(region)
            .build();
    }

    public String getSecret(String secretName)
    {
        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
            .withSecretId(secretName);
        GetSecretValueResult getSecretValueResult = null;

        try
        {
            logger.info("Calling AWS SecretsManager");
            getSecretValueResult = awsSecretsManagerClient.getSecretValue(getSecretValueRequest);
        }
        catch (DecryptionFailureException e)
        {
            logger.error("Secrets Manager can't decrypt the protected secret text using the provided KMS key.");
            throw e;
        }
        catch (InternalServiceErrorException e)
        {
            logger.error("An error occurred on the server side.");
            throw e;
        }
        catch (InvalidParameterException e)
        {
            logger.error("An invalid value was provided for a parameter.");
            throw e;
        }
        catch (InvalidRequestException e)
        {
            logger.error("A parameter value was provided that is not valid for the current state of the resource.");
            throw e;
        }
        catch (ResourceNotFoundException e)
        {
            logger.error("Can't find the resource that was requested.");
            throw e;
        }

        // Returns decrypted secret using the associated KMS CMK.
        return getSecretValueResult.getSecretString();
    }

    private AWSCredentialsProvider getAwsCredentialsProvider(Boolean useLocalCredentials)
    {
        if (useLocalCredentials)
        {
            return new ProfileCredentialsProvider(new ProfilesConfigFile("../.aws/credentials"), "default");
        }
        else
        {
            return new InstanceProfileCredentialsProvider(false);
        }
    }
}
