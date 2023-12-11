# Dockerfile to create image with cron services
FROM ubuntu:latest
MAINTAINER iamg.herold@gmail.com

# Add the script to the Docker Image
ADD copp_clark.jar /src/main/java/za/co/rmb/copp_clark_uat_uploader/CoppClark.java

# Install Cron and Java
# Install Cron and Java
RUN apt-get update && \
    apt-get -y install software-properties-common && \
    add-apt-repository ppa:openjdk-r/ppa && \
    apt-get update && \
    apt-get -y install cron openjdk-8-jdk

# Add the cron job
RUN crontab -l | { cat; echo "0 5 * * * java -jar /copp_clark.jar"; } | crontab -

# Run the command on container startup
CMD cron
