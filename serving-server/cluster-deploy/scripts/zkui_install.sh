#!/bin/bash
set -e
source ./allinone_cluster_configurations.sh
zkui_path=$deploy_dir/fate-serving/common
zkui_url=https://github.com/DeemOpen/zkui.git
if [ ! -d ${zkui_path} ]
then
 mkdir -p ${zkui_path}
fi
cd ${zkui_path}
git clone $zkui_url
cd zkui
mvn clean package -DskipTests
mv target/zkui-2.0-SNAPSHOT-jar-with-dependencies.jar ./
