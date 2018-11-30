package moccasin.moccasin;

import java.io.IOException;
import java.util.logging.LogManager;

import io.helidon.microprofile.server.Server;

/**
 * Main method simulating trigger of main method of the server.
 */
public final class Main {

    private Main() { }

    /**
     * Application main entry point.
     * @param args command line arguments
     * @throws IOException if there are problems reading logging properties
     */
    public static void main(final String[] args) throws IOException {
        setupLogging();
        startServer();
    }

    /**
     * Start the server.
     * Server will automatically pick up configuration from
     * microprofile-config.properties and Application classes annotated as @ApplicationScoped
     * @return the created {@link Server} instance
     */
    static Server startServer() {
        return Server.create().start();
    }

    /**
     * Configure logging from logging.properties file.
     */
    private static void setupLogging() throws IOException {
           LogManager.getLogManager().readConfiguration(Main.class.getResourceAsStream("/logging.properties"));
    }
}
