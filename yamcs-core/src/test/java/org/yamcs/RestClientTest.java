package org.yamcs;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.yamcs.api.rest.RestClient;
import org.yamcs.api.ws.YamcsConnectionProperties;
import org.yamcs.protobuf.YamcsManagement.YamcsInstance;

public class RestClientTest  extends AbstractIntegrationTest {
    
    @Test
    public void testGetYamcsInstancesUnauthorized() throws Exception {
        YamcsConnectionProperties ycp = new YamcsConnectionProperties("localhost", 9190);
        
        RestClient restClient = new RestClient(ycp);
        Exception e = null;
        try {
            restClient.getYamcsInstances();
        } catch (Exception e1) {
            e=e1;
        }
        assertNotNull(e);
        assertTrue(e.getMessage().contains("401 Unauthorized"));
    }

    @Test
    public void testGetYamcsInstances() throws Exception {
        YamcsConnectionProperties ycp = new YamcsConnectionProperties("localhost", 9190);
        ycp.setAuthenticationToke(admin);
        RestClient restClient = new RestClient(ycp);
        List<YamcsInstance> instances = restClient.getYamcsInstances();
        
        assertEquals(1, instances.size());
        assertEquals("IntegrationTest", instances.get(0).getName());
    }
    
    
    @Test
    public void testGetBulk() throws Exception {
        YamcsConnectionProperties ycp = new YamcsConnectionProperties("localhost", 9190);
        ycp.setAuthenticationToke(admin);
        RestClient restClient = new RestClient(ycp);
        List<YamcsInstance> instances = restClient.getYamcsInstances();
        
        assertEquals(1, instances.size());
        assertEquals("IntegrationTest", instances.get(0).getName());
    }
}
