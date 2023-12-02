package za.co.rmb.copp_clark_uat_uploader;

import com.jcraft.jsch.*;
import za.co.rmb.copp_clark_uat_uploader.interfaces.Variables;

import java.io.*;
import java.util.List;
public class ProdProcessor implements Variables {
    public Session createSession(String user, String pass, String host) throws JSchException {
        JSch jSch = new JSch();
        Session session = jSch.getSession(user, host, 22);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setPassword(pass);
        session.connect();

        return session;
    }
    public void UploadFilesProd(List<File> files, String user, String pass, String host)
            throws SftpException, IOException, JSchException, InterruptedException {
        Session session = createSession(user, pass, host);
        if (session.isConnected()) {
            System.out.println("INFO:: Session just connected - " + session.getHost());
        }
        Thread.sleep(1000);

        Channel shell = session.openChannel("shell");                                                              // Initializing the shell command line
        OutputStream outStream = shell.getOutputStream();
        shell.setOutputStream(System.out, true);                                                               // Redirect the shell output to System.out
        shell.connect();
        if (shell.isConnected()) {
            System.out.println("INFO:: Shell just connected - " + shell.getSession());
        }
        Thread.sleep(1000);

        Channel sftp = session.openChannel("sftp");
        ChannelSftp sftpChannel = (ChannelSftp) sftp;
        sftpChannel.connect();
        if (sftpChannel.isConnected()) {
            System.out.println("INFO:: Server File Transfer Protocol just connected - " + shell + "\n");
        }
        Thread.sleep(1000);

        System.out.println("INFO:: Changing directory to 'importer/updates'\n");
        sftpChannel.cd("importer/updates");
        System.out.println("INFO:: Working on Prod: files will be uploaded at - " + sftpChannel.pwd() + "\n");

        for (File file : files) {
            sftpChannel.put(file.getAbsolutePath(), ".");
            System.out.println("-- INFO:: Successfully copied file: " + file.getName() + " to - " + sftpChannel.pwd() + "\n");
        }
        System.out.println("INFO:: Changing directory to root");
        sftpChannel.cd("/");

        InputStream inputStream = new ByteArrayInputStream(prodCommands.getBytes());
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            int length;
            try {
                while ((length = inputStream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, length);
                    System.out.println("INFO: executing commands ");
                    outStream.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        ChannelExec exec = (ChannelExec) session.openChannel("exec");
        exec.setCommand(prodCommands);
        exec.setCommand("exit");
        exec.connect();

        shell.disconnect();
        System.out.println("INFO:: ssh command line disconnected");
        Thread.sleep(1000);

        sftpChannel.disconnect();
        System.out.println("INFO:: sftp channel disconnected");
        Thread.sleep(1000);

        exec.disconnect();
        System.out.println("INFO:: execution channel disconnected");
        Thread.sleep(1000);

        session.disconnect();
        System.out.println("INFO:: ssh session: "+ session.getHost() +" disconnected");
//        String receiver = "iamg.herold@gmail.com";
//        String sender = "930204given@gmail.co.za";
//        String emailHost = "smtp.gmail.com";
//        EmailNotifier notifier = new EmailNotifier(receiver, sender, emailHost);
//        notifier.sendEmail("Hey given this is a test email\nRegards,\nGiven Mutshidza", "copp clark test");
    }
}
