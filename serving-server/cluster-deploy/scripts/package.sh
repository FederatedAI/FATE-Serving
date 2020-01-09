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
fate_serving_proxy_zip=fate-serving-proxy-*-release.zip
fate_serving_proxy_jar=fate-serving-proxy-*.jar
serving_proxy=serving-proxy
serving_proxy_path=$fate_serving_path/serving-proxy
echo "--------------------start"
if [ ! -d $fate_serving_path  ]; then
	mkdir -p $fate_serving_path
	if [ ! -d $sering_path  ]; then
		cd $fate_serving_path
		mkdir $serving
		mkdir $serving_proxy
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
cd $cwd/../../../$serving_proxy/target
cp $fate_serving_proxy_zip $serving_proxy_path
cd $serving_proxy_path
unzip $fate_serving_proxy_zip
ln -s $fate_serving_proxy_jar fate-serving-proxy.jar
cd  $sering_path/conf
sed -i 's#redis.ip=127.0.0.1#'redis.ip=${host_redis_ip}'#' serving-server.properties
sed -i 's#redis.port=6379#'redis.port=${host_redis_port}'#' serving-server.properties
sed -i 's#redis.password=fate_dev#'redis.password=${host_redis_password}'#' serving-server.properties
sed -i "/^workMode=/cworkMode=${workMode}" serving-server.properties
sed -i "/^model.transfer.url=/cmodel.transfer.url=${host_model_transfer}/v1/model/transfer" serving-server.properties
cd $cwd
if [ ${apply_zk} = "false" ]
then
	cd $serving_proxy_path/conf
	sed -i "/^useRegister=/cuseRegister=false" application.properties
	sed -i "/^useZkRouter=/cuseZkRouter=false" application.properties
	sed -i "/^route.table=/croute.table=${deploy_dir}/fate-serving/serving-proxy/conf/route_table.json" application.properties
	sed -i "/^auth.file=/cauth.file=${deploy_dir}/fate-serving/serving-proxy/conf/auth_config.json" application.properties
	sed -i "6c \"ip\":\"${host_guest[1]}\"," route_table.json
	sed -i 's#10000#'${party_list[0]}'#'  route_table.json
    sed -i '7c "prot":8000' route_table.json
    sed -i "17a ,\"serving\":[{\"ip\":\"${host_guest[0]}\",\"prot\":8080}]" route_table.json
	cd  $sering_path/conf
    sed -i "/^useRegister=/cuseRegister=false" serving-server.properties
    sed -i "/^useZkRouter=/cuseZkRouter=false" serving-server.properties
elif [ ${apply_zk} = "true" ]
then
	echo "---------apply _zk true"
	cd $serving_proxy_path/conf
	echo "----------------" $pwd
	sed -i "/^zk.url=/czk.url=${host_zk_url}" application.properties
	sed -i "/^useRegister=/cuseRegister=true" application.properties
    sed -i "/^useZkRouter=/cuseZkRouter=true" application.properties
	sed -i "/^route.table=/croute.table=${deploy_dir}/fate-serving/serving-proxy/conf/route_table.json" application.properties
    sed -i "/^auth.file=/cauth.file=${deploy_dir}/fate-serving/serving-proxy/conf/auth_config.json" application.properties
	sed -i 's#10000#'${party_list[0]}'#'  route_table.json
	cd  $sering_path/conf
	sed -i "/^zk.url=/czk.url=${host_zk_url}" serving-server.properties
	sed -i "/^useRegister=/cuseRegister=true" serving-server.properties
    sed -i "/^useZkRouter=/cuseZkRouter=true" serving-server.properties
else
		echo ""
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
		scp ${public_path}/fate-serving.tar.gz ${host_guest[1]}:${public_path}
        ssh -tt ${user}@${host_guest[1]} << eeooff
	cd ${public_path}
	echo "public_path-------" ${public_path}
	tar -zxvf fate-serving.tar.gz
	rm -rf fate-serving.tar.gz
	cd ${public_path}/fate-serving/serving/conf
	sed -i "/^zk.url=/czk.url=${guest_zk_url}" serving-server.properties
	sed -i 's#redis.ip=${host_redis_ip}#'redis.ip=${guest_redis_ip}'#' serving-server.properties
	sed -i 's#redis.port=${host_redis_port}#'redis.port=${guest_redis_port}'#' serving-server.properties
	sed -i 's#redis.password=${host_redis_password}#'redis.password=${guest_redis_password}'#' serving-server.properties
	sed -i "/^model.transfer.url=/cmodel.transfer.url=${guest_model_transfer}/v1/model/transfer" serving-server.properties
	cd ${serving_proxy_path}/conf
    sed -i '/^zk.url=/czk.url=${guest_zk_url}' application.properties
	sed -i '6c "ip":"${host_guest[0]}",' route_table.json
	sed -i 's#${party_list[0]}#'${party_list[1]}'#'  route_table.json
	sed -i '7c "prot":8000' route_table.json
	grep serving route_table.json >/dev/null
	if [ $? -eq 0 ]
	then
		sed -i '18d' route_table.json
		sed -i "18i ,\"serving\":[{\"ip\":\"${host_guest[1]}\",\"prot\":8080}]" route_table.json
	fi
exit
eeooff

}

if [[ ${host_guest[1]} ]]
then
        echo "------guest hava host_guest1  -----is not null"
        if [[ ${host_guest[1]} != "" ]]; then
                init_env
        else
                echo "please input guest from allinone_cluster_configurations"
                exit 0
        fi

else
        echo "no have guest--------false   no null"
fi
