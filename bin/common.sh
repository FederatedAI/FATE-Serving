#!/bin/bash
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
    if [[ ! -e "fate-${module}.jar" ]]; then
      ln -s fate-${module}-${module_version}.jar fate-${module}.jar
    fi
    if [ ${module} = "serving-server" ]; then
      java -Dspring.config.location=${configpath}/serving-server.properties -cp "conf/:lib/*:extension/*:fate-${module}.jar" ${main_class} >>logs/console.log 2>>logs/error.log &
    elif [ ${module} = "serving-proxy" ]; then
      java -Dspring.config.location=${configpath}/application.properties -cp "conf/:lib/*:fate-${module}.jar" ${main_class} >>logs/console.log 2>>logs/error.log &
    elif [ ${module} = "serving-admin" ]; then
      java -Dspring.config.location=${configpath}/application.properties -cp "conf/:lib/*:fate-${module}.jar" ${main_class} >>logs/console.log 2>>logs/error.log &
    else
      echo "usage: ${module} {serving-server|serving-proxy|serving-admin|serving-monitor}"
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