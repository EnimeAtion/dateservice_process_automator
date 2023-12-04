package za.co.rmb.copp_clark_uat_uploader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import za.co.rmb.copp_clark_uat_uploader.file_manager.FileManager;
import za.co.rmb.copp_clark_uat_uploader.processing.UATProcessor;
import za.co.rmb.copp_clark_uat_uploader.utilities.Utilities;

public class CoppClark {
    private static final Logger logger = LogManager.getLogger(CoppClark.class);
    public static void main(String[] args) throws Exception {

        ProdProcessor prod = new ProdProcessor();
        UATProcessor uat = new UATProcessor();
        logger.info("Copp Clark Prod/UAT Uploader just started . . .\n");
        logger.info("Java Version: " + System.getProperty("java.version"));
        logger.info("Configuration Path: " + Utilities.getXMLFilePath());
        FileManager.siteLogin();
        //FileManager.moveFiles();
        //EmailNotifier.sendEmail("Test", "This is the testing message.");
        //logger.info("The folder to lookup : " + FileManager.dateFolder);


    }

}