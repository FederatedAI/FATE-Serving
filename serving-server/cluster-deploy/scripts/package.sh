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

mkdir -p ./${fate_serving}/${serving}
mkdir -p ./${fate_serving}/${serving_proxy}
cp services.sh ./${fate_serving}
cd ${cwd}/../../../
echo "[INFO] mvn clean package  start"
mvn clean package -DskipTests
if [ $? -eq 0 ]; then
	echo  "[INFO]  mvn clean package success"
else
	echo "[INFO]  mvn clan package filed"
	exit
fi

#serving
cd ${cwd}/../../target
cp ${fate_serving_zip} ${cwd}/${fate_serving}/${serving}
cd ${cwd}/${fate_serving}/${serving}
unzip $fate_serving_zip
ln -s $fate_serving_jar  fate-serving-server.jar

#serving-proxy
cd ${cwd}/../../../$serving_proxy/target
cp $fate_serving_proxy_zip ${cwd}/${fate_serving}/${serving_proxy}
cd ${cwd}/${fate_serving}/${serving_proxy}
unzip $fate_serving_proxy_zip
ln -s $fate_serving_proxy_jar fate-serving-proxy.jar

#cp modify_json.py
cd ${cwd}
cp modify_json.py ./fate-serving/serving-proxy/conf
tar -zcvf fate-serving.tar.gz  fate-serving

cp_serving() {
	for node_ip in "${host_guest[@]}"; do
	echo "[INFO] install on ${node_ip}"
	ssh -tt ${user}@${node_ip} << eeooff
if [ ! -d ${deploy_dir}  ]; then
	mkdir -p ${deploy_dir}
fi
exit
eeooff
scp fate-serving.tar.gz ${user}@${node_ip}:${deploy_dir}
	ssh -tt ${user}@${node_ip} << eeooff
	cd ${deploy_dir}
	tar -zxvf fate-serving.tar.gz
	rm -rf fate-serving.tar.gz
	exit
eeooff
	done
}
cp_serving
if [ $? -eq 0 ]; then
	echo  "[INFO]  cp fate-serving.tar.gz success"
else
	echo "[INFO]  cp fate-serving.tar.gz filed"
	exit
fi


update_config () {
    for ((i=0;i<${#host_guest[*]};i++))
    do
		#update serving-proxy config path
		ssh -tt ${user}@${host_guest[i]} << eeooff
		cd ${deploy_dir}/fate-serving/serving-proxy/conf
		sed -i "/^route.table=/croute.table=${deploy_dir}/fate-serving/serving-proxy/conf/route_table.json" application.properties
		sed -i "/^auth.file=/cauth.file=${deploy_dir}/fate-serving/serving-proxy/conf/auth_config.json" application.properties
exit
eeooff


		temp=$(( $i % 2 ))
		if [ $temp = 0 ]
		then
		ssh -tt ${user}@${host_guest[i]} << eeooff
		cd ${deploy_dir}/fate-serving/serving/conf
		sed -i "/^redis.ip=/credis.ip=${host_redis_ip}" serving-server.properties
		sed -i "/^redis.port=/credis.port=${host_redis_port}" serving-server.properties
		sed -i "/^redis.password=/credis.password=${host_redis_password}" serving-server.properties
		sed -i "/^workMode=/cworkMode=${workMode}" serving-server.properties
		sed -i "/^model.transfer.url=/cmodel.transfer.url=${host_fate_flow_url}/v1/model/transfer" serving-server.properties
exit
eeooff
		else
		ssh -tt ${user}@${host_guest[i]} << eeooff
		cd ${deploy_dir}/fate-serving/serving/conf
		sed -i "/^redis.ip=/credis.ip=${guest_redis_ip}" serving-server.properties
		sed -i "/^redis.port=/credis.port=${guest_redis_port}" serving-server.properties
		sed -i "/^redis.password=/credis.password=${guest_redis_password}" serving-server.properties
		sed -i "/^workMode=/cworkMode=${workMode}" serving-server.properties
		sed -i "/^model.transfer.url=/cmodel.transfer.url=${guest_fate_flow_url}/v1/model/transfer" serving-server.properties
exit
eeooff
		fi
    done


}
update_config

#apply zookeepre the config
update_zk_config () {
	for ((i=0;i<${#host_guest[*]};i++))
    do
		temp=$(( $i % 2 ))
		if [ $temp = 0 ]
		then
			ssh -tt ${user}@${host_guest[i]} << eeooff
				cd ${deploy_dir}/fate-serving/serving/conf
				sed -i "/^zk.url=/czk.url=${host_zk_url}" serving-server.properties
				sed -i "/^useRegister=/cuseRegister=true" serving-server.properties
				sed -i "/^useZkRouter=/cuseZkRouter=true" serving-server.properties
				cd ${deploy_dir}/fate-serving/serving-proxy/conf
				sed -i "/^zk.url=/czk.url=${host_zk_url}" application.properties
				sed -i "/^useRegister=/cuseRegister=true" application.properties
				sed -i "/^useZkRouter=/cuseZkRouter=true" application.properties
exit
eeooff
		else
			ssh -tt ${user}@${host_guest[i]} << eeooff
				cd ${deploy_dir}/fate-serving/serving/conf
				sed -i "/^zk.url=/czk.url=${guest_zk_url}" serving-server.properties
				sed -i "/^useRegister=/cuseRegister=true" serving-server.properties
				sed -i "/^useZkRouter=/cuseZkRouter=true" serving-server.properties
				cd ${deploy_dir}/fate-serving/serving-proxy/conf
				sed -i "/^zk.url=/czk.url=${guest_zk_url}" application.properties
				sed -i "/^useRegister=/cuseRegister=true" application.properties
				sed -i "/^useZkRouter=/cuseZkRouter=true" application.properties
exit
eeooff
		fi
	done
}

#no zookeepre the config
update_nozk_config () {
	for ((i=0;i<${#host_guest[*]};i++))
    do
		      	ssh -tt ${user}@${host_guest[i]} << eeooff
                        cd ${deploy_dir}/fate-serving/serving/conf
                        sed -i "/^useRegister=/cuseRegister=false" serving-server.properties
                        sed -i "/^useZkRouter=/cuseZkRouter=false" serving-server.properties
                        cd ${deploy_dir}/fate-serving/serving-proxy/conf
                        sed -i "/^useZkRouter=/cuseZkRouter=false" application.properties
exit
eeooff
		temp=$(( $i % 2 ))
		if [ $temp = 0 ]
		then
			ssh -tt ${user}@${host_guest[i]} << eeooff
			cd ${deploy_dir}/fate-serving/serving-proxy/conf
			sed -i "3c default_default_ip=\"${host_guest[i+1]}\"" modify_json.py
			sed -i "5c party_id=${party_list[i]}" modify_json.py
			sed -i "8c role_serving_ip=\"${host_guest[i]}\"" modify_json.py
			sed -i "9c role_serving_port=8080" modify_json.py
			python modify_json.py route_table.json
			rm -rf modify_json.py
exit
eeooff
		else
			ssh -tt ${user}@${host_guest[i]} << eeooff
			cd ${deploy_dir}/fate-serving/serving-proxy/conf
			sed -i "3c default_default_ip=\"${host_guest[i-1]}\"" modify_json.py
			sed -i "5c party_id=${party_list[i]}" modify_json.py
			sed -i "8c role_serving_ip=\"${host_guest[i]}\"" modify_json.py
			sed -i "9c role_serving_port=8080" modify_json.py
			python modify_json.py route_table.json
			rm -rf modify_json.py
exit
eeooff
		fi
	done
}

if [ ${apply_zk} = "true" ]
then
	update_zk_config
elif [ ${apply_zk} = "false" ]
then
	update_nozk_config
else
	echo ""
fi
rm -rf fate-serving*
