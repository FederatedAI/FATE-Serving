#!/bin/bash

BASE_SERVER=https://webank-ai-1251170195.cos.ap-guangzhou.myqcloud.com/
jdk_version=jdk-8u192-linux-x64.tar.gz
jdk=jdk-8u192-linux-x64
yum install -y wget
wget $BASE_SERVER/${jdk_version}
tar -zxvf ${jdk_version} -C /usr/local
cat >> /etc/profile << EOF
export JAVA_HOME=/usr/local/${jdk}
export PATH=\$PATH:\$JAVA_HOME/bin
EOF
#source /etc/profile

#cat >> /etc/profile << EOF
#export JAVA_HOME=/usr/local/${jdk}
#export PATH=\$PATH:\$JAVA_HOME/bin
#EOF
