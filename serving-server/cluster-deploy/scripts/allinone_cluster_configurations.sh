#!/bin/bash
user=app
host_guest=(172.16.153.9 172.16.153.113)
roll_hostAndguest=(172.16.153.9 172.16.153.113)
deploy_dir=/data/projects
host_redis_ip=127.0.0.2
host_redis_port=63792
host_redis_password=fate_dev_host
guest_redis_ip=127.0.0.3
guest_redis_port=63793
guest_redis_password=fate_dev_guest
apply_zk=true
host_zk_url=zookeeper://localhost:2182
guest_zk_url=zookeeper://localhost:2183
workMode=1
