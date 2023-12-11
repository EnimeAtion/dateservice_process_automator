package za.co.rmb.copp_clark_uat_uploader.file_manager;

import com.microsoft.playwright.*;
import za.co.rmb.copp_clark_uat_uploader.constants_reader.ConstantsReader;
import za.co.rmb.copp_clark_uat_uploader.exceptions.FilesTableEmptyException;
import za.co.rmb.copp_clark_uat_uploader.exceptions.PageNotFoundException;
import za.co.rmb.copp_clark_uat_uploader.interfaces.Variables;
import za.co.rmb.copp_clark_uat_uploader.utilities.Utilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.*;
import java.nio.file.*;
import java.util.regex.*;

public class FileManager implements Variables {
    private static final Logger logger = LogManager.getLogger(FileManager.class);
    public static void siteLogin() throws Exception {

        ConstantsReader constantsReader = new ConstantsReader();

        try (Playwright playwright = Playwright.create()) {
            BrowserType browserType = playwright.chromium();
            Browser browser = browserType.launch();
            Browser.NewContextOptions options = new Browser.NewContextOptions();
            options.setAcceptDownloads(true);
            BrowserContext context = browser.newContext(options);
            Page page = context.newPage();

            page.onDownload(download -> {
                deleteOldFiles();
                logger.info("Download started: " + download.url());
                Path downloadPath = Paths.get("C:\\Users\\r5667771\\Documents\\dateservice\\" + download.suggestedFilename());
                download.saveAs(downloadPath);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                logger.info("Download saved to: " + downloadPath);
            });
            page.setDefaultTimeout(240_000);

            page.navigate(constantsReader.getUrl("copp"));
            logger.info("Accessing " + constantsReader.getUrl("copp"));

            page.fill(userID, constantsReader.getUsername("copp"));
            logger.info("Inserting username into a field !!");

            page.fill(pwdID, constantsReader.getPassword("copp"));
            logger.info("Inserting password into a field !!");

            page.click(loginID, new Page.ClickOptions().setTimeout(6000));
            logger.info("Clicking the login button !!");

            if (!page.title().equals(title)) {
                throw new PageNotFoundException("Couldn't login to the website, check your credentials and network, and try again");
            } else {
                logger.info("Successfully Logged in to " + page.title());
            }

            Thread.sleep(10000);
            if (page.querySelector(tableCSS) != null) {
                ElementHandle newFilesTable = page.querySelector(tableCSS);
                if (!newFilesTable.querySelector(filenameCss).toString().matches(fileRegex)) {
                    throw new FilesTableEmptyException("No xml files present!");
                } else {
                    logger.info("Accessing the new files table");
                }

                for (ElementHandle row : newFilesTable.querySelectorAll(rowCss)) {
                    String filename = row.querySelector(filenameCss).innerText();

                    if(filename.matches(fileRegex)) {
                        logger.info("File: " + filename + " matches the regex: " + fileRegex);
                        row.waitForSelector(downloadBtnCss);
                        Thread.sleep(5000);
                        ElementHandle downloadBtn = row.querySelector(downloadBtnCss);
                        downloadBtn.click();
                        page.waitForSelector(header);
                        page.querySelector(modalClose).click();

                    }
                }

            }
            else {
                throw new FilesTableEmptyException("NewFilesTable not found (no new files available)!");
            }

            browser.close();
        }
    }
    public static void deleteOldFiles() {
        Path downloadPath = Paths.get("C:\\Users\\r5667771\\Documents\\dateservice\\");

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(downloadPath)) {
            for (Path file: stream) {
                Files.delete(file);
            }
            logger.info("Old files deleted successfully");
        } catch (IOException e) {
            logger.warn("No old files to delete!");
        }

    }

}
