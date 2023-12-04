package za.co.rmb.copp_clark_uat_uploader.file_manager;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import za.co.rmb.copp_clark_uat_uploader.constants_reader.ConstantsReader;
import za.co.rmb.copp_clark_uat_uploader.interfaces.Variables;
import za.co.rmb.copp_clark_uat_uploader.utilities.Utilities;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.*;

public class FileManager implements Variables {
    public static String dateFolder = "";
    private static final Logger logger = LogManager.getLogger(FileManager.class);
    public static void siteLogin() throws Exception {

        ConstantsReader constantsReader = new ConstantsReader();
        ChromeOptions options = getChromeOptions();

        WebDriverManager.firefoxdriver().setup();
        WebDriver driver = new RemoteWebDriver(new URL(dockerUrl), options);

        driver.get(constantsReader.getUrl("copp"));
        logger.info("Accessing " + constantsReader.getUrl("copp"));

        WebElement userField = driver.findElement(By.id(userID));
        WebElement pwdField = driver.findElement(By.id(pwdID));

        userField.sendKeys(constantsReader.getUsername("copp"));
        logger.info("Inserting username into a field !!");

        pwdField.sendKeys(constantsReader.getPassword("copp"));
        logger.info("Inserting password into a field !!");

        WebElement loginBtn = driver.findElement(By.id(loginID));
        loginBtn.click();
        logger.info("Clicking the login button !!");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        wait.until(ExpectedConditions.titleIs(title));

        logger.info("Logged in to " + driver.getTitle());

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("newFilesTable")));

        WebElement newFilesTable = null;
        if (driver.findElement(By.id("newFilesTable")).isDisplayed()) {
            newFilesTable = driver.findElement(By.id("newFilesTable"));
        }
        else {
            throw new Exception("The no new files found . . . ");
        }

        logger.info("Accessing the newFilesTable");
        assert newFilesTable != null;
        for (WebElement row : newFilesTable.findElements(By.cssSelector(rowCss))) {
            if(row.findElement(By.cssSelector(filenameCss)).getText().trim().matches(fileRegex) ) {
                String filename = row.findElement(By.cssSelector(filenameCss)).getText().trim();

                Matcher matcher = filePattern.matcher(filename);
                if (matcher.matches()) {
                    logger.info("File found: " + filename);
                    WebElement downloadButton = row.findElement(By.xpath(downloadBtnCss));
                    dateFolder = Utilities.removeDateFromFilename(filename);
                    createFolder(dateFolder);
                    logger.info("The date of the file is: " + dateFolder);

                    logger.info("File: " + filename + " matches the file pattern - " + filePattern);
                    logger.info("Downloading " + filename + "  . . .");

                    Thread.sleep(500);
                    logger.info("Download clicked . . .");
                    downloadButton.click();
                    logger.info("Waiting for download to finish . . . ");
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(header)));
                    driver.findElement(By.cssSelector(modalClose)).click();
                    wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className(header)));

                    ProcessBuilder processBuilder = new ProcessBuilder("wget", "--progress=dot", "-O", filename, driver.getCurrentUrl());
                    processBuilder.redirectErrorStream(true);
                    Process process = processBuilder.start();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        logger.info(line);
                    }
                    process.waitFor();
                    Thread.sleep(5000);
                }

            }
            else {
                logger.error("\nNo matching file found! - " + fileRegex);
                logger.info("closing the program . . . \n");
                Thread.sleep(5000);
                driver.quit();
                break;

            }

        }

        Thread.sleep(5000);
        driver.quit();

        moveFiles();
    }

    private static ChromeOptions getChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-gpu");
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", "/path/to/save/files");
        prefs.put("profile.default_content_setting_values.automatic_downloads", 1);
        options.setExperimentalOption("prefs", prefs);

        return options;
    }

    public static void createFolder(String date) {

        String docPath = Utilities.getPathToSaveFiles().toString();
        File dateService = new File(docPath + File.separator );
        boolean dateserviceCreated = dateService.mkdir();
        if (dateserviceCreated) {
            logger.info("Dateservice folder has been created");
        }

        File dateFolder = new File(dateService.getPath() + File.separator + date);
        boolean isDateCreated = dateFolder.mkdir();
        if (isDateCreated) {
            logger.info("New folder have been created: " + date);
        }
    }

    public static void moveFiles() {
        Path filesDir = Utilities.getPathToSaveFiles();

        logger.info("Copying files from " + Utilities.getPathToSaveFiles() + " to 'importer/updates' ");

        Pattern filePattern = Pattern.compile(fileRegex);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(filesDir)) {
            for (Path file : stream) {
                String filename = file.getFileName().toString();
                Matcher matcher = filePattern.matcher(filename);

                if (matcher.matches()) {
                    String date = Utilities.removeDateFromFilename(filename);
                    createFolder(date);
                    Path targetDir = filesDir.resolve(date);
                    Path targetFile = targetDir.resolve(filename);
                    Files.move(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                    logger.info("Moved file: " + filename + " to " + targetFile);
                } else {
                    logger.error("This is not dateservice file - " + filename);
                }

                if (filename.equals("copp_clark_uat_uploader-0.0.1-Beta-jar-with-dependencies.jar")) {
                    System.out.println("Jar file found!!! - " +filename);

                    ProcessBuilder processBuilder = new ProcessBuilder();
                    processBuilder.command("shell", "java -jar "+ filename);
                    processBuilder.start();
                }
            }
        } catch (IOException e) {
            logger.error("Error while trying to search and move files from Downloads folder: " + e.getMessage());
            e.printStackTrace();
        }
    }



}
