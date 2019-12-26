#!/bin/bash
set -e
source ./allinone_cluster_configurations.sh
zk_version=zookeeper-3.4.14
common_path=$deploy_dir/fate-serving/common
if [ ! -d $common_path  ]; then
	echo "[INFO]    mkdir -p $common_path"
	mkdir -p $common_path
else
  echo [INFO] $fate_serving_path dir exist
fi
cd $common_path

#zk的压缩包名字
zk_tar=$zk_version.tar.gz

url=https://mirrors.tuna.tsinghua.edu.cn/apache/zookeeper/$zk_version/$zk_version.tar.gz

#下载zk
wget $url
#解压 
tar -xvf $zk_tar
rm -rf $zk_tar
cd $zk_version
#创建日志文件夹
mkdir logs
mkdir datar

zk_path=$(cd `dirname $0`; pwd)
cd conf
echo "sh  zkServer.sh start ------"  $sh  zkServer.sh start
cp zoo_sample.cfg zoo.cfg
sed -i 's#/tmp/zookeepe#'$zk_path'/data#' zoo.cfg
sed -i '/^dataDir/a\dataLogDir='$zk_path'/logs\' zoo.cfg
sed -i '$a\admin.serverPort=9080' zoo.cfg
#cd ../bin
#sh zkServer.sh start
#if [ $? -eq 0 ]; then
#	echo  "[INFO]  zookeeper start success"
#else
#	echo "[INFO]  zookeeper start  filed"
#fi
