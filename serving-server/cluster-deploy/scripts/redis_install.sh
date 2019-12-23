﻿#!/bin/bash
#------------------------------------------------------------------------------------------------------------------#
#|          Some people die at the age of 25 and don't bury themselves in the earth until they are 75             |#
#------------------------------------------------------------------------------------------------------------------#
#|                      $$$$ $$   $$ $$$$$$ $$    $$   $$$$$$          $$     $$$$$$ $$$$$$                       |#
#|                     $$    $$   $$ $$     $$ $$ $$  $$               $$     $$     $$                           |#
#|                    $$     $$$$$$$ $$$$$  $$  $$ $ $$  $$$$$$        $$     $$$$$  $$$$$                        |#
#|                     $$    $$   $$ $$     $$   $ $  $$     $$        $$     $$     $$                           |#
#|                      $$$$ $$   $$ $$$$$$ $$    $$   $$$$$ $$        $$$$$$ $$$$$$ $$$$$$                       |#
#------------------------------------------------------------------------------------------------------------------#
cwd=$(cd `dirname $0`; pwd)
onversion="4.0.3"
offversion=`basename redis-*.tar.gz .tar.gz | awk -F '-' '{print$2}'`
installdir=$(cd `dirname $0`; pwd)
 
function initize(){
    installdir=$(cd `dirname $0`; pwd)
}
 
function  checkroot(){
if [ $UID -ne  0 ]
  then
    echo "|----------------------------------------------------------------------------------------------------------------|"
    echo "|------------------------------------------[权限不足...请切换至root用户]-----------------------------------------|"
    echo "|----------------------------------------------------------------------------------------------------------------|"
    exit;
fi
}
 
function judge(){
    echo
    offfile=`ls | grep redis-*.tar.gz`
    if [[ "$offfile" != "" ]]
    then
        echo "|----------------------------------------------------------------------------------------------------------------|"
        echo "|-------------------------------------------------[发现离线包]---------------------------------------------------|"
        echo "|----------------------------------------------------------------------------------------------------------------|"
        /usr/bin/sleep 3
        offinstall
    else
        echo "|----------------------------------------------------------------------------------------------------------------|"
        echo "|-------------------------------------------------[未发现离线包]-------------------------------------------------|"
        echo "|--------------------------------------------[开始判断是否连接外网安装]------------------------------------------|"
        /usr/bin/sleep 3
        network
    fi
}
 
function offinstall(){
    echo "|----------------------------------------------------------------------------------------------------------------|"
    echo "|------------------------------------------------[离线包安装中]--------------------------------------------------|"
    echo "|----------------------------------------------------------------------------------------------------------------|"
    tar -zxvf redis-${offversion}.tar.gz >/dev/null 2>&1
    redis="redis-${offversion}"
    cd ${redis}/src && make >/dev/null 2>&1
    if [[ $? -ne 0 ]]; then
        echo "编译出错"
    else
        echo "|----------------------------------------------------------------------------------------------------------------|"
        echo "|---------------------------------------------------[编译完成]---------------------------------------------------|"
        echo "|----------------------------------------------------------------------------------------------------------------|"
        /usr/bin/sleep 3
        intend
    fi
}
 
function network(){
    httpcode=`curl -I -m 10 -o /dev/null -s -w %{http_code}'\n' http://www.baidu.com`
    net1=$(echo $httpcode | grep "200")
    if [[ "$net1" != "" ]]
    then
        echo "|----------------------------------------------------------------------------------------------------------------|"
        echo "|-----------------------------------------------------[联网]-----------------------------------------------------|"
        echo "|-------------------------------------------------[准备联网安装]-------------------------------------------------|"
        echo "|----------------------------------------------------------------------------------------------------------------|"
        /usr/bin/sleep 3
        online
    else
        echo "|----------------------------------------------------------------------------------------------------------------|"
        echo "|-------------------------------------------[未联网,无离线安装包,准备退出]---------------------------------------|"
        echo "|----------------------------------------------------------------------------------------------------------------|"
        /usr/bin/sleep 3
        exit;
    fi
}
function online(){
    wget_v=`which wget`
    wget_vv=$(echo $wget_v | grep wget)
    if [[ "$wget_vv" != "" ]]
    then
        echo "|----------------------------------------------------------------------------------------------------------------|"
        echo "|--------------------------------------`wget -V |head -n 1`---------------------------------------|"
        echo "|----------------------------------------------------------------------------------------------------------------|"
        wget http://download.redis.io/releases/redis-${onversion}.tar.gz
        installon
    else
        echo "|----------------------------------------------------------------------------------------------------------------|"
        echo "|----------------------------------------[检测到wget没有安装, 准备安装wget]---------------------------------------|"
        echo "|----------------------------------------------------------------------------------------------------------------|"
        yum install wget -y
        echo "|----------------------------------------------------------------------------------------------------------------|"
        echo "|--------------------------------------`wget -V |head -n 1`---------------------------------------|"
        echo "|----------------------------------------------------------------------------------------------------------------|"
        wget http://download.redis.io/releases/redis-${onversion}.tar.gz
        installon
    fi
}
 
function installon(){
    echo "|----------------------------------------------------------------------------------------------------------------|"
    echo "|------------------------------------------------[在线包安装中]--------------------------------------------------|"
    echo "|----------------------------------------------------------------------------------------------------------------|"
    tar -zxvf redis-${onversion}.tar.gz >/dev/null 2>&1
    redis="redis-${onversion}"
    cd ${redis}/src && make >/dev/null 2>&1
    if [[ $? -ne 0 ]]; then
        echo "编译出错"
    else
        echo "|----------------------------------------------------------------------------------------------------------------|"
        echo "|--------------------------------------------------[编译完成]----------------------------------------------------|"
        echo "|----------------------------------------------------------------------------------------------------------------|"
        /usr/bin/sleep 3
        intend
    fi
}
 
function intend(){
    echo "|----------------------------------------------------------------------------------------------------------------|"
    echo "|-------------------------------------------------[开始迁移文件]-------------------------------------------------|"
    echo "|----------------------------------------------------------------------------------------------------------------|"
    mkdir -p ${installdir}/redis/{logs,nodes,conf,bin}
    cp redis-cli redis-server ${installdir}/redis/bin
    cp redis-trib.rb ${installdir}/redis
    cp ../redis.conf ${installdir}/redis/conf
    echo "|----------------------------------------------------------------------------------------------------------------|"
    echo "|-------------------------------------------------[数据迁移完成]-------------------------------------------------|"
    echo "|----------------------------------------------------------------------------------------------------------------|"
    /usr/bin/sleep 2
    echo "|----------------------------------------------------------------------------------------------------------------|"
    echo "|-------------------------------------------------[清理多余文件]-------------------------------------------------|"
    echo "|----------------------------------------------------------------------------------------------------------------|"
    finish
    /usr/bin/sleep 2
    echo "|----------------------------------------------------------------------------------------------------------------|"
    echo "|-------------------------------------------------[配置快捷启动]-------------------------------------------------|"
    echo "|----------------------------------------------------------------------------------------------------------------|"
    service
    /usr/bin/sleep 2
    echo "|----------------------------------------------------------------------------------------------------------------|"
    echo "|-------------------------------------------------[修改配置文件]-------------------------------------------------|"
    echo "|----------------------------------------------------------------------------------------------------------------|"
    configfile
    /usr/bin/sleep 2
    echo "|****************************************************************************************************************|"
    echo "|            WW             WW EEEEEEE LL     CCCCC   OOOOOO      MM      MM     EEEEEEE                         |"
    echo "|             WW    WWWW   WW  EE      LL    CC      OO    OO    MMMM    MMMM    EE                              |"
    echo "|              WW  WW WW  WW   EEEEE   LL   CC      OO      OO  MM  MM  MM  MM   EEEEE                           |"
    echo "|               WW W   W WW    EE      LL    CC      OO    OO  MM    M M     MM  EE                              |"
    echo "|                WW     WW     EEEEEEE LLLLLL CCCCC   OOOOOO  MM     MMM      MM EEEEEEE                         |"
    echo "|****************************************************************************************************************|"
}
function finish(){
    echo
    rm -rf ${installdir}/redis-*
}
function service(){
    cd ${installdir}/redis && echo "./bin/redis-server conf/redis.conf" > start.sh
    chmod +x start.sh
}
function configfile(){
    cd ${installdir}/redis/conf
    #后台
    sed -i 's/daemonize no/daemonize yes/' redis.conf
    #端口
    #日志输出文件
    sed -i.bak "s/bind 127.0.0.1/bind 0.0.0.0/g" ./redis.conf
    sed -i 's/logfile ""/logfile "\/usr\/local\/src\/redis\/logs\/redis.logs"/' redis.conf
}
function main(){
checkroot
judge
}
main

#cd $cwd/redis/bin
#echo "$cwd--------" $cwd
#echo "[INFO] --------redis start" 
#./redis-server &
