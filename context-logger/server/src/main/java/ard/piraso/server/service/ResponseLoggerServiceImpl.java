package ard.piraso.server.service;

import ard.piraso.api.IDGenerator;
import ard.piraso.api.Preferences;
import ard.piraso.api.entry.Entry;
import ard.piraso.api.io.PirasoEntryWriter;
import ard.piraso.server.logger.TraceableID;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Response logger service implementation.
 */
public class ResponseLoggerServiceImpl implements ResponseLoggerService {

    /**
     * final class logging instance.
     */
    private static final Log LOG = LogFactory.getLog(ResponseLoggerServiceImpl.class);

    /**
     * If queue size reaches this size, the service will auto stop.
     */
    private static final int MAX_QUEUE_FORCE_KILL_SIZE = 2000;

    /**
     * Maximum of 30 minutes idle time, otherwise the service will auto stop.
     */
    private static final long MAX_IDLE_TIME_KILL_SIZE = 30 * 60 * 1000;

    /**
     * Service instance id generator.
     */
    private static final IDGenerator ID_GENERATOR = new IDGenerator();

    /**
     * Request parameter name for the remote monitored address.
     */
    private static final String MONITORED_ADDR_PARAMETER = "monitoredAddr";

    /**
     * Request parameter name for the logging preferences.
     */
    private static final String PREFERENCES_PARAMETER = "preferences";

    /**
     * The response content type.
     */
    private static final String RESPONSE_CONTENT_TYPE = "xml/plain";

    /**
     * The transfer queue. This holds the queue which will be streamed to response writer.
     */
    private List<TransferEntryHolder> transferQueue = Collections.synchronizedList(new LinkedList<TransferEntryHolder>());

    /**
     * Service instance id.
     */
    private long id = ID_GENERATOR.next();

    /**
     * The user which monitors.
     */
    private User user;

    /**
     * The user monitor remote address.
     */
    private String monitoredAddr;

    /**
     * The response transfer.
     */
    private HttpServletResponse response;

    /**
     * Responsible for writing transfer entries to response stream writer.
     */
    private PirasoEntryWriter writer;

    /**
     * Determines whether the service is still active or not.
     */
    private boolean alive = true;

    /**
     * The user logging preference.
     */
    private Preferences preferences;

    /**
     * Determiens the current idle time. If this exceed the {@link #MAX_IDLE_TIME_KILL_SIZE} the service will be forced
     * stopped.
     */
    private long currentIdleTime = 0l;

    /**
     * Construct the service given the user, request and response.
     *
     * @param user the monitor {@link User}.
     * @param request the current request object.
     * @param response the current response object.
     * @throws IOException on io error
     */
    public ResponseLoggerServiceImpl(User user, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Validate.notNull(request.getParameter(MONITORED_ADDR_PARAMETER), String.format("Request parameter '%s' is required", MONITORED_ADDR_PARAMETER));
        Validate.notNull(request.getParameter(PREFERENCES_PARAMETER), String.format("Request parameter '%s' is required", PREFERENCES_PARAMETER));

        this.user = user;
        this.response = response;
        this.monitoredAddr = request.getParameter(MONITORED_ADDR_PARAMETER);
        this.preferences = new ObjectMapper().readValue(request.getParameter(PREFERENCES_PARAMETER), Preferences.class);
    }

    /**
     * {@inheritDoc}
     */
    public User getUser() {
        return user;
    }

    /**
     * {@inheritDoc}
     */
    public String getMonitoredAddr() {
        return monitoredAddr;
    }

    /**
     * {@inheritDoc}
     */
    public long getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAlive() throws IOException {
        return alive;
    }

    /**
     * {@inheritDoc}
     */
    public void start() throws IOException, TransformerConfigurationException, ParserConfigurationException {
        response.setContentType(RESPONSE_CONTENT_TYPE);

        try {
            writer = new PirasoEntryWriter(getId(), response.getWriter());
            doLogWhileAlive();
        } finally {
            synchronized (this) {
                if(writer != null) {
                    writer.close();
                }

                notifyAll();
            }
        }
    }

    /**
     * Wait for {@code 1800000l} millis if not interrupted for a log. This will ensure that the owning thread will idle
     * when the {@link #transferQueue} is empty.
     * <p>
     * This is also responsible for computing for the idle time, which when the idle time exceeds the limit
     * {@link #MAX_IDLE_TIME_KILL_SIZE} the service will forced stopped.
     *
     * @throws IOException on io error
     */
    private void waitTillNoEntry() throws IOException {
        if(transferQueue.isEmpty()) {
            try {
                wait(1800000l);
                currentIdleTime += 1800000l;

                if(currentIdleTime > MAX_IDLE_TIME_KILL_SIZE) {
                    stop();
                }
            } catch (InterruptedException ignored) {
            }
        }
    }

    /**
     * Empty the transfer queue and write to response stream writer. This will also reset the {@link #currentIdleTime}
     * to {@code 0}.
     */
    private void writeAllTransferFromQueue() {
        while(CollectionUtils.isNotEmpty(transferQueue)) {
            try {
                TransferEntryHolder holder = transferQueue.remove(0);

                writer.write(holder.id.toString(), holder.entry);
                currentIdleTime = 0;
            } catch (Exception e) {
                LOG.warn(e.getMessage(), e);
            }
        }
    }

    /**
     * Ensure to wait and do log while still not stopped or was forced to stop.
     *
     * @throws IOException on io error
     */
    private void doLogWhileAlive() throws IOException {
        synchronized (this) {
            while(alive) {
                waitTillNoEntry();
                writeAllTransferFromQueue();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void stop() throws IOException {
        synchronized(this) {
            alive = false;
            notifyAll();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void stopAndWait(long timeout) throws InterruptedException {
        synchronized(this) {
            if(alive) {
                wait(timeout);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public Preferences getPreferences() {
        return preferences;
    }

    /**
     * {@inheritDoc}
     */
    public void log(TraceableID id, Entry entry) throws IOException {
        synchronized (this) {
            if(transferQueue.size() > MAX_QUEUE_FORCE_KILL_SIZE) {
                stop();

                return;
            }

            transferQueue.add(new TransferEntryHolder(id, entry));
            notifyAll();
        }
    }
}