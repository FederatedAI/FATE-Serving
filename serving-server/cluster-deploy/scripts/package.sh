#!/bean package -DskipTestsin/bash
#
#Copyright 2019 The serving Authors. All Rights Reserved.
#

set -e
source ./allinone_cluster_configurations.sh
cwd=$(cd `dirname $0`; pwd)
cd ${cwd}
public_path=$deploy_dir
package_path=$cwd/FATE-Serving
fate_serving_path=$public_path/fate-serving
sering_path=$fate_serving_path/serving
fate_serving=fate-serving
serving=serving
fate_serving_zip=fate-serving-server-*-release.zip
fate_serving_jar=fate-serving-server-*.jar
router_zip=fate-serving-router-*-release.zip
router_jar=fate-serving-router-*.jar
common=$public_path/fate-serving/common
router=router
router_path=$fate_serving_path/router

if [ ! -d $common  ]; then
	mkdir -p $common
else
  echo [INFO] $common dir exist
fi
cp jdk_install.sh $common
cp redis_install.sh $common
cp services.sh $fate_serving_path

cd $common
echo "[INFO] jdk install"
#sudo sh jdk_install.sh
#sudo rm -rf jdk_install.sh
echo "[INFO] redis install"
sudo sh redis_install.sh
sudo rm -rf redis_install.sh
cd redis/conf
sudo sed -i.bak "s/# requirepass foobared/requirepass ${redis_password}/g" ./redis.conf
sudo sed -i.bak "s/databases 16/databases 50/g" ./redis.conf

cd ${cwd}/../../../
echo "[INFO] mvn clean package  start"
mvn clean package -DskipTests
if [ $? -eq 0 ]; then
	echo  "[INFO]  mvn clean package success"
else
	echo "[INFO]  mvn clan package filed"
fi

if [ ! -d $fate_serving_path  ]; then
	cd $public_path
	mkdir $fate_serving
else
  echo [INFO] $fate_serving_path dir exist
fi

if [ -d $fate_serving_path  ]; then
	if [ ! -d $sering_path  ]; then
		cd $fate_serving_path
		mkdir $serving
		mkdir $router
	else
		echo [INFO] $fate_serving_path dir exist
	fi
	
else
  echo ""
fi

cd $cwd/../../target
cp $fate_serving_zip $sering_path
if [ $? -eq 0 ]; then
	echo  "[INFO]  cp fate-serving-server-*-release.zip success"
else
	echo "[INFO]  cp fate-serving-server-*-release.zip filed"
fi
cd $sering_path
unzip $fate_serving_zip
ln -s $fate_serving_jar  fate-serving-server.jar

echo "[INFO]  cp fate-serving-router-*-release.zip success"
cd $cwd/../../../router/target
cp $router_zip $router_path
if [ $? -eq 0 ]; then
	echo  "[INFO]  cp fate-serving-router-*-release.zip success"
else
	echo "[INFO]  cp fate-serving-server-*-release.zip filed"
fi
echo "-------------------routerpach"
cd $router_path
unzip $router_zip
ln -s $router_jar fate-serving-router.jar
echo "--------------------ln -s"
cd $cwd
cp  start_env/services.sh $public_path/fate-serving/common
echo "------------apply----" ${apply_zk}

if [ ${apply_zk} = "false" ]
then
	cd $router_path/conf
	sed -i 's#useRegister=true#'useRegister=false'#' proxy.properties
	sed -i 's#useZkRouter=true#'useZkRouter=false'#' proxy.properties
	cd  $sering_path/conf
	sed -i 's#useRegister=true#'useRegister=false'#' serving-server.properties
	sed -i 's#useZkRouter=true#'useZkRouter=false'#' serving-server.properties
fi
	sh zookeeper_install.sh
	cd $cwd
	echo "-------zkui_instal start"
	sh zkui_install.sh

sudo cp redis/service.sh  $common/redis
cp zk/service.sh  $common/zookeeper*
cp zkui/service.sh  $common/zkui
echo "---------------------sed roll"
if [[ ${host_guest[0]} ]]
then
	cd ${public_path}/fate-serving/serving/conf
	sed -i 's#127.0.0.1:8011#'${roll_hostAndguest[0]}'#' serving-server.properties
else
        echo "no have host--------false   no null"
		exit
fi

init_env() {
        ssh -tt ${user}@${host_guest[1]}  << eeooff
	if [ ! -d ${public_path} ]
	then
		mkdir -p ${public_path}
	fi
	exit
eeooff
		cd ${public_path}
		tar -zcvf fate-serving.tar.gz  fate-serving/

		scp ${public_path}/fate-serving.tar.gz @${host_guest[1]}:${public_path}
        ssh -tt ${user}@${host_guest[1]} << eeooff
	cd ${public_path}
	echo "public_path-------" ${public_path}
	tar -zxvf fate-serving.tar.gz
	rm -rf fate-serving.tar.gz
	cd ${public_path}/fate-serving/serving/conf
	sed -i 's#${roll_hostAndguest[0]}#'${roll_hostAndguest[1]}'#' serving-server.properties
exit
eeooff

}

echo "host_guest1--if-----" ${host_guest[1]}
if [[ ${host_guest[1]} ]]
then
        echo "------guest hava host_guest1  -----is not null"
        if [[ ${roll_hostAndguest[1]} != "" ]]; then
                init_env
        else
                echo "please input guest from allinone_cluster_configurations"
                exit 0
        fi

else
        echo "no have guest--------false   no null"
fi




