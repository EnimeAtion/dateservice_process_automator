package za.co.rmb.copp_clark_uat_uploader.processor;

import za.co.rmb.copp_clark_uat_uploader.constants_reader.ConstantsReader;
import za.co.rmb.copp_clark_uat_uploader.interfaces.Variables;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Processor implements Variables {
    private static final Logger logger = LogManager.getLogger(Processor.class);
    /**
     * Processes the files
     * @param files - list of files to process
     */
    public static void fileProcessing(List<File> files) throws Exception {
        ConstantsReader constantsReader = new ConstantsReader();
        try {
            InetAddress inet = InetAddress.getLocalHost();
            String hostname = inet.getHostName();

            ProcessBuilder processBuilder = new ProcessBuilder();

            //When the host name is PROD
            if (hostname.equals(constantsReader.getHostname("prod_server"))) {
                try {
                    logger.info("Running on " + hostname);

                    processBuilder.command("bash", "cd importer/updates");
                    logger.info("Changing Directory to 'importer/updates'");

                    for (File file : files) {
                        processBuilder.command("bash", "cp", file.getAbsolutePath(), "importer/updates");
                        logger.info("Copying " + file.getName() + " to 'importer/updates'");
                    }
                    processBuilder.command("bash", "cd /");
                    logger.info("Changing Directory one step back.");

                    processBuilder.command("bash", prodCommands);
                    logger.info("Running the 'update.sh' script and generating logs");
                    Process process = processBuilder.start();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }

                    int exitCode = process.waitFor();
                    System.out.println("\nExited with error code : " + exitCode);
                }
                catch(IOException | InterruptedException ex) {
                    logger.error("An error has occured: " + ex.getMessage());
                    ex.printStackTrace();
                }

            }

            //When the host name is UAT
            if (hostname.equals(constantsReader.getHostname("test_server"))) {
                logger.info("Running on " + hostname);

                processBuilder.command("bash", "-c", uatCommands);
            }

        }
        catch(UnknownHostException ex) {
            logger.error("The hostname doesn't match the server name");
        }

    }

}
