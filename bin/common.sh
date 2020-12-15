#!/bin/bash

#
#  Copyright 2019 The FATE Authors. All Rights Reserved.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#

set -e
getpid() {
  if [ -e "./bin/${module}.pid" ]; then
    pid=$(cat ./bin/${module}.pid)
  fi
  if [[ -n ${pid} ]]; then
    count=$(ps -ef | grep $pid | grep -v "grep" | wc -l)
    if [[ ${count} -eq 0 ]]; then
      rm ./bin/${module}.pid
      unset pid
    fi
  fi
}

mklogsdir() {
  if [[ ! -d "logs" ]]; then
    mkdir logs
  fi
}

start() {
  echo "try to start ${module}"
  getpid
  if [[ ! -n ${pid} ]]; then
    mklogsdir

    if [[ -e "fate-${module}.jar" ]]; then
      rm fate-${module}.jar
    fi
    ln -s fate-${module}-${module_version}.jar fate-${module}.jar

    if [ ${module} = "serving-server" ]; then
      JAVA_OPT="${JAVA_OPT} -Dspring.config.location=${configpath}/serving-server.properties"
      JAVA_OPT="${JAVA_OPT} -cp conf/:lib/*:extension/*:fate-${module}.jar"
    elif [ ${module} = "serving-proxy" ] || [ ${module} = "serving-admin" ]; then
      JAVA_OPT="${JAVA_OPT} -Dspring.config.location=${configpath}/application.properties"
      JAVA_OPT="${JAVA_OPT} -cp conf/:lib/*:fate-${module}.jar"
    else
      echo "usage: ${module} {serving-server|serving-proxy|serving-admin}"
    fi

    JAVA_OPT="${JAVA_OPT} ${main_class}"
    if [[ $1 == "front" ]]; then
      exec java ${JAVA_OPT}
    else
      java ${JAVA_OPT} >logs/console.log 2>logs/error.log &
    fi

    #sleep 5
    #id=$(ps -p $! | awk '{print $1}' | sed -n '2p')
    inspect_pid 5 $!

    if [[ "$exist" = 1 ]]; then
      echo $! >./bin/${module}.pid
      getpid
      echo "service start sucessfully. pid: ${pid}"
    else
      echo "service start failed"
    fi
  else
    echo "service already started. pid: ${pid}"
  fi
}

status() {
  getpid
  if [[ -n ${pid} ]]; then
    echo "status: $(ps -f -p ${pid})"
    exit 0
  else
    echo "service not running"
    exit 1
  fi
}

stop() {
  getpid
  if [[ -n ${pid} ]]; then
    echo "killing: $(ps -p ${pid})"
    kill ${pid}
    if [[ $? -eq 0 ]]; then
      #此函数检查进程，判断进程是否存在
      echo "please wait"
      inspect_pid 5 ${pid}
      if [[ "$exist" = 0 ]]; then
        echo "killed"
      else
        echo "please retry"
      fi
    else
      echo "kill error"
    fi
  else
    echo "service not running"
  fi
}


inspect_pid() {
  total=0
  exist=0
  #echo "inspect pid: $2,periods: $1"
  if [[ -n $2 ]]; then
    while [[ $total -le $1 ]]
    do
      count=$(ps -ef | grep $2 | grep -v "grep" | wc -l)
      total=$(($total+1))
      if [[ ${count} -ne 0 ]]; then
        sleep 1
        exist=1
       else
        exist=0
        return
       fi
    done
  fi
}