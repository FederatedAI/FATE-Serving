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
router=router
router_path=$fate_serving_path/router

if [ ! -d $fate_serving_path  ]; then
	mkdir -p $fate_serving_path
	if [ ! -d $sering_path  ]; then
		cd $fate_serving_path
		mkdir $serving
		mkdir $router
	else
		echo [INFO] $fate_serving_path dir exist
	fi
fi
cp services.sh $fate_serving_path
cd ${cwd}/../../../
echo "[INFO] mvn clean package  start"
mvn clean package -DskipTests
if [ $? -eq 0 ]; then
	echo  "[INFO]  mvn clean package success"
else
	echo "[INFO]  mvn clan package filed"
	exit
fi


cd $cwd/../../target
cp $fate_serving_zip $sering_path
cd $sering_path
unzip $fate_serving_zip
ln -s $fate_serving_jar  fate-serving-server.jar
cd $cwd/../../../router/target
cp $router_zip $router_path
cd $router_path
unzip $router_zip
ln -s $router_jar fate-serving-router.jar
echo "--------------------ln -s"
cd  $sering_path/conf
sed -i 's#redis.ip=127.0.0.1#'redis.ip=${host_redis_ip}'#' serving-server.properties
sed -i 's#redis.port=6379#'redis.port=${host_redis_port}'#' serving-server.properties
sed -i 's#redis.password=fate_dev#'redis.password=${host_redis_password}'#' serving-server.properties
sed -i "/^workMode=/cworkMode=${workMode}" serving-server.properties
echo "------------------------- sed"
cd $cwd
echo "-------------------- cwd" $cwd
if [ ${apply_zk} = "false" ]
then
	echo "---------- apply_zk false"
	cd $router_path/conf
	sed -i 's#useRegister=true#'useRegister=false'#' proxy.properties
	sed -i 's#useZkRouter=true#'useZkRouter=false'#' proxy.properties
	cd  $sering_path/conf
	sed -i 's#useRegister=true#'useRegister=false'#' serving-server.properties
	sed -i 's#useZkRouter=true#'useZkRouter=false'#' serving-server.properties
elif [ ${apply_zk} = "true" ]
then
	echo "---------apply _zk true"
	cd $router_path/conf
	sleep 6
	echo "----------------" ${host_zk_url}
	echo "----------------" ${host_zk_url}
	echo "----------------" ${host_zk_url}
	echo "----------------" ${host_zk_url}
	echo "----------------" ${host_zk_url}
	echo "----------------" ${host_zk_url}
	echo "----------------" pwd
	echo "----------------" $pwd
	sed -i "/^zk.url=/czk.url=${host_zk_url}" proxy.properties
	cd  $sering_path/conf
	sed -i 's#zk.url=zookeeper://localhost:2181#'zk.url=${host_zk_url}'#' serving-server.properties
else 
		echo ""
fi
echo "------------- zk uil"
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
		echo "-----------------scp-:"  ${host_guest[1]}
		scp ${public_path}/fate-serving.tar.gz ${host_guest[1]}:${public_path}
        ssh -tt ${user}@${host_guest[1]} << eeooff
	cd ${public_path}
	echo "public_path-------" ${public_path}
	tar -zxvf fate-serving.tar.gz
	rm -rf fate-serving.tar.gz
	cd ${public_path}/fate-serving/serving/conf
	sed -i 's#${roll_hostAndguest[0]}#'${roll_hostAndguest[1]}'#' serving-server.properties
	sed -i 's#zk.url=${host_zk_url}#'zk.url=${guest_zk_url}'#' serving-server.properties
	sed -i 's#redis.ip=${host_redis_ip}#'redis.ip=${guest_redis_ip}'#' serving-server.properties
	sed -i 's#redis.port=${host_redis_port}#'redis.port=${guest_redis_port}'#' serving-server.properties
	sed -i 's#redis.password=${host_redis_password}#'redis.password=${guest_redis_password}'#' serving-server.properties
	 cd ${router_path}/conf
       	sed -i '/^zk.url=/czk.url=${guest_zk_url}' proxy.properties
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




