#!/bin/bash
sudo su - dateservice
cd importer/updates
ll -larth /tmp/Copp_Clark/
cp /tmp/Copp_Clark/* .
cd ..
./update.sh
cd /
exit
