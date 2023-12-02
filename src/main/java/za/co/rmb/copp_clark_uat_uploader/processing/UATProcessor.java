package za.co.rmb.copp_clark_uat_uploader.processing;

import com.jcraft.jsch.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import za.co.rmb.copp_clark_uat_uploader.interfaces.Variables;

import java.io.*;
import java.util.List;
import java.util.Vector;
import java.io.IOException;
public class UATProcessor implements Variables {

    public static final Logger LOGGER = LogManager.getLogger(UATProcessor.class);
    /**
     * Connects to the server
     *
     * @param user - login username
     * @param pass - login password
     * @param host - login hostname
     * @return - session
     * @throws JSchException -
     */
    public Session createSession(String user, String pass, String host) throws JSchException {
        JSch jSch = new JSch();
        Session session = jSch.getSession(user, host, 22);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setPassword(pass);
        session.connect();

        return session;
    }

    /**
     * Process the files to UAT server
     *
     * @param files - files to process
     * @param user - login username
     * @param pass - login password
     * @param host - login lost
     * @throws SftpException -
     * @throws IOException -
     * @throws JSchException -
     * @throws InterruptedException -
     */
    public void UploadFilesAndExecuteCommands(List<File> files, String user, String pass, String host)
            throws SftpException, IOException, JSchException, InterruptedException {

        Session session = createSession(user, pass, host);

        if (session.isConnected()) {
            System.out.println();
            LOGGER.info("session just connected: " + session.getHost());
        }
        Thread.sleep(1500);

        Channel shell = session.openChannel("shell");
        OutputStream outStream = shell.getOutputStream();
        shell.setOutputStream(System.out, true);
        shell.connect();
        LOGGER.info("ssh command line channel connected");
        Thread.sleep(1000);

        Channel channel = session.openChannel("sftp");
        ChannelSftp sftpChannel = (ChannelSftp) channel;
        sftpChannel.connect();
        LOGGER.info("sftp channel connected");
        Thread.sleep(1000);

        LOGGER.info("file transfer process started.");
        sftpChannel.cd("/");
        sftpChannel.cd("/tmp/Copp_Clark");
        LOGGER.info("files will be copied to: " + sftpChannel.pwd());

        Vector<ChannelSftp.LsEntry> filesList = sftpChannel.ls(".");
        LOGGER.info("removing old files in '/tmp/Copp_Clark/'");
        for (ChannelSftp.LsEntry file : filesList) {
            if (!file.getAttrs().isDir()) {
                LOGGER.info("-- " + file.getFilename() + " removed successfully");

                sftpChannel.rm(file.getFilename());
            }
        }
        Thread.sleep(1000);

        LOGGER.info("changing file permission for our new files.");
        for (File file : files) {
            try {
                sftpChannel.put(file.getAbsolutePath(), ".");
                // Change permissions of each file to 664
                sftpChannel.chmod(436, file.getName());
                LOGGER.info("-- successfully changed " + file + " file permissions.");
            } catch (SftpException ex) {
                LOGGER.error("-- failed to change file permissions.");
                throw new RuntimeException(ex);
            }
        }
        Thread.sleep(1000);

        InputStream inputStream = new ByteArrayInputStream(uatCommands.getBytes());
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            int length;
            try {
                while ((length = inputStream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, length);
                    outStream.flush();
                }
            } catch (IOException e) {
                LOGGER.error("couldn't run the command");
                e.printStackTrace();
            }
        }).start();
        sftpChannel.exit();
    }


}

