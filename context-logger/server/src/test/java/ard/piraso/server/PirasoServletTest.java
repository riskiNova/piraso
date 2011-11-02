package ard.piraso.server;

import ard.piraso.api.Preferences;
import ard.piraso.server.service.ResponseLoggerService;
import ard.piraso.server.service.User;
import ard.piraso.server.service.UserRegistry;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import static ard.piraso.server.CommonMockObjects.mockRequest;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Test for {@link PirasoServlet} class.
 */
public class PirasoServletTest {
    public static final String MONITORED_ADDR = "127.0.0.1";

    private UserRegistry registry;

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    private PirasoServlet servlet;

    private ObjectMapper mapper;

    @Before
    public void setUp() throws Exception {
        registry = spy(new UserRegistry());
        request = mockRequest(MONITORED_ADDR);
        response = spy(new MockHttpServletResponse());
        mapper = new ObjectMapper();
        mapper.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);

        request.addParameter("activity_uuid", "1");
        servlet = new PirasoServlet();
        servlet.setRegistry(registry);
    }

    @Test
    public void testNullServiceParameter() throws Exception {
        servlet.handleRequest(request, response);

        assertTrue(response.getErrorMessage().contains("required"));
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void testInvalidServiceParameter() throws Exception {
        request.addParameter("service", "notvalid");
        servlet.handleRequest(request, response);

        assertTrue(response.getErrorMessage().contains("invalid"));
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void testStopNullService() throws Exception {
        request.addParameter("service", "stop");
        request.addParameter("preferences", mapper.writeValueAsString(new Preferences()));

        doReturn(null).when(registry).getLogger(Matchers.<User>any());

        servlet.handleRequest(request, response);

        assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
    }

    @Test
    public void testStopNotAliveService() throws Exception {
        request.addParameter("service", "stop");
        request.addParameter("preferences", mapper.writeValueAsString(new Preferences()));

        ResponseLoggerService service = mock(ResponseLoggerService.class);

        doReturn(false).when(service).isAlive();
        doReturn(service).when(registry).getLogger(Matchers.<User>any());

        servlet.handleRequest(request, response);

        assertEquals(HttpServletResponse.SC_CONFLICT, response.getStatus());

    }

    @Test
    public void testStoptimeout() throws Exception {
        servlet.setStopTimeout(100l);

        request.addParameter("service", "stop");
        request.addParameter("preferences", mapper.writeValueAsString(new Preferences()));

        ResponseLoggerService service = mock(ResponseLoggerService.class);

        doReturn(true).when(service).isAlive();
        doReturn(service).when(registry).getLogger(Matchers.<User>any());

        servlet.handleRequest(request, response);

        assertEquals(HttpServletResponse.SC_REQUEST_TIMEOUT, response.getStatus());
    }

    @Test
    public void testStartStopSuccess() throws IOException, ServletException, ExecutionException, InterruptedException {
        final AtomicBoolean fail = new AtomicBoolean(false);
        request.addParameter("service", "start");
        request.addParameter("preferences", mapper.writeValueAsString(new Preferences()));

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Runnable startServiceRunnable = new Runnable() {
            public void run() {
                try {
                    servlet.handleRequest(request, response);
                } catch (Exception e) {
                    fail.set(true);
                    e.printStackTrace();
                }
            }
        };

        Runnable logMessagesRunnable = new Runnable() {
            public void run() {
                try {
                    MockHttpServletRequest request = mockRequest(MONITORED_ADDR);
                    request.addParameter("activity_uuid", "1");
                    request.addParameter("service", "stop");

                    // wait till service is available
                    while(registry.getLogger(registry.createOrGetUser(request)) == null) {
                        Thread.sleep(100l);
                    }

                    servlet.handleRequest(request, new MockHttpServletResponse());
                } catch (Exception e) {
                    fail.set(true);
                    e.printStackTrace();
                }
            }
        };

        Future future = executor.submit(startServiceRunnable);
        executor.submit(logMessagesRunnable);

        future.get();
        executor.shutdown();

        verify(registry).removeUser(registry.createOrGetUser(request));
    }

    @Test
    public void testStartMaxIdleTimeoutAndMaxForceSize() throws Exception {
        request.addParameter("service", "start");
        request.addParameter("preferences", mapper.writeValueAsString(new Preferences()));

        servlet.setMaxIdleTimeout(100l);
        servlet.setMaxQueueForceKillSize(2);
        servlet.handleRequest(request, response);

        verify(registry).removeUser(registry.createOrGetUser(request));
    }
}
