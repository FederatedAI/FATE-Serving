#!/bin/bash
user=app
host_guest=(172.16.153.9 172.16.153.113)
roll_hostAndguest=(172.16.153.9 172.16.153.113)
deploy_dir=/data/projects
host_redis_ip=127.0.0.1
host_redis_port=63792
host_redis_password=fate_dev
guest_redis_ip=127.0.0.1
guest_redis_port=6379
guest_redis_password=fate_dev
apply_zk=true
host_zk_url=zookeeper://localhost:2181
guest_zk_url=zookeeper://localhost:2181
workMode=1
