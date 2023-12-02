package za.co.rmb.copp_clark_uat_uploader.utilities;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Utilities {

    public static Session createSession(String user, String pass, String host) throws JSchException {
        JSch jSch = new JSch();
        Session session = jSch.getSession(user, host, 22);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setPassword(pass);
        session.connect();

        return session;
    }

    public static Path getXMLFilePath() {
        return Paths.get(System.getProperty("user.dir") + "\\src\\main\\resources\\constants\\Constants.xml");
    }

    public static Path getPathToSaveFiles() {
        return Paths.get(System.getProperty("user.home") +"\\Documents\\dateservice");
    }
    public static Path getChromePath() {
        return Paths.get("\\src\\main\\resources\\browser\\firefox_win\\core\\firefox.exe");
    }

    public static Path getChromeDriverPath() {
        return Paths.get("\\src\\main\\resources\\chrome_driver\\chromedriver.exe");
    }
    public static String removeDateFromFilename(String filename) {
        return filename.substring(7, 15);
    }
}
