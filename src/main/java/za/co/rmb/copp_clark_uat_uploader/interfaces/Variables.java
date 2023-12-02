package za.co.rmb.copp_clark_uat_uploader.interfaces;

import java.util.regex.Pattern;

public interface Variables {

    String fileRegex = "Update_(\\d{8})\\((Bank|Currency|ExchTrdg|ExchSett)\\)\\.xml";

    Pattern filePattern = Pattern.compile(fileRegex);

    String uatCommands = "sudo -u dateservice 'cd importer/updates ; " +
            "cp /tmp/Copp_Clark/* . ; ll ; cd .. ;" +
            " ./update.sh > $(if [ $(date +%u) -eq 1 ]; then date -d \"-3 day\" +%d-%m-%y; else date -d \"-1 day\" +%d-%m-%y; fi)_log.txt ;" +
            " cd updates ; ll ; exit ; exit'";

    String prodCommands = "/home/RMB-NT/s_rac_datesrv/importer/update.sh > /home/RMB-NT/s_rac_datesrv/importer/$(if [ $(date +%u) -eq 1 ]; then date -d \"-3 day\" +%d-%m-%y; else date -d \"-1 day\" +%d-%m-%y; fi)_log.txt 0>&1";
    String filenameCss = "td:nth-child(1) > a > span";
    String rowCss = "tbody > tr";
    String downloadBtnCss = ".//*[@id[starts-with(.,'download-file-')]]";
    String title = "Copp Clark Limited: Rand Merchant Bank";
    String dockerUrl = "http://localhost:4444/wd/hub";
    String userID = "form_username";
    String pwdID = "form_password";
    String loginID = "submit_button";
    String modalClose = ".modal-controls .header-control";
    String header = "modal-header";
}
