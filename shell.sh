#!/bin/sh
cd /Users/juzheng/IDEA_CheckOut/ys_log_dev_3_0_logstream_server
gradle build -x test
ssh root@172.17.30.10 "cd /YSApp/;rm -rf server-3.2.4-SNAPSHOT;rm -rf server-3.2.4-SNAPSHOT.tar;rm -rf client-3.2.4-SNAPSHOT;rm -rf client-3.2.4-SNAPSHOT.tar;exit"
scp /Users/juzheng/IDEA_CheckOut/ys_log_dev_3_0_logstream_server/server/build/distributions/server-3.2.4-SNAPSHOT.tar root@172.17.30.10:/YSApp
ssh root@172.17.30.10 "cd /YSApp/;tar -xvf server-3.2.4-SNAPSHOT.tar;tar -xvf client-3.2.4-SNAPSHOT.tar;ln -s server-3.2.4-SNAPSHOT ys_log_server;systemctl restart ys_log_server;tail -f /YSLog/ys_log_server/log/run.log"
