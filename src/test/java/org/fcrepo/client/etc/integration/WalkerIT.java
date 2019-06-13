/*
 * Licensed to DuraSpace under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * DuraSpace licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fcrepo.client.etc.integration;

import static javax.ws.rs.core.Response.Status.CREATED;
import static org.fcrepo.client.etc.Walker.TEXT_TURTLE;
import static org.junit.Assert.assertEquals;

import org.apache.commons.io.IOUtils;
import org.fcrepo.client.FcrepoClient;
import org.fcrepo.client.FcrepoOperationFailedException;
import org.fcrepo.client.FcrepoResponse;
import org.fcrepo.client.TestUtils;
import org.fcrepo.client.etc.Walker;
import org.fcrepo.client.etc.WalkerConfig;
import org.fcrepo.client.integration.AbstractResourceIT;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;

/**
 * @author awoods
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/spring-test/test-container.xml")
public class WalkerIT extends AbstractResourceIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(WalkerIT.class);

    private static final WalkerConfig WALKER_CONFIG = new WalkerConfig(serverAddress, "fedoraAdmin", "password");
    private FcrepoClient client;
    private Walker walker;

    
    public WalkerIT() {
        super();

        walker = new Walker(WALKER_CONFIG);
        client = FcrepoClient.client()
                .credentials("fedoraAdmin", "password")
                .authScope("localhost")
                .build();
    }

    @Test
    public void testGetChildren() throws Exception {
        // Create some children
        final int numChildren = 4;
        for (int x = 0; x < numChildren; ++x) {
            postResource(new ByteArrayInputStream(TestUtils.rdfTtl.getBytes()));
        }

        // Verify all children plus the base are found
        assertEquals(numChildren + 1, walker.run());
    }

    private void postResource(final InputStream body) throws Exception {
        try (final FcrepoResponse response = client.post(new URI(serverAddress)).body(body, TEXT_TURTLE).perform()) {
            final String content = IOUtils.toString(response.getBody(), "UTF-8");
            LOGGER.info("Received response: {}", content);
            final int status = response.getStatusCode();
            assertEquals("Didn't get a CREATED response! Got content:\n" + content, CREATED.getStatusCode(), status);
        }
    }

}
