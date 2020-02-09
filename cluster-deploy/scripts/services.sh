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

modules=(serving-proxy serving-server)

cwd=`pwd`

main() {
    module=$1
    action=$2
    echo "--------------"
    echo "[INFO] processing: ${module}"
	cd ${module}
	bash service.sh ${action}
    echo "--------------"
    echo
}

all() {
    echo "[INFO] processing all modules"
    echo "=================="
    echo
    for module in "${modules[@]}"; do
        cd ${cwd}
        main ${module} $2
    done
    echo "=================="
}

usage() {
    echo "usage: $0 {all|[module1, ...]} {start|stop|status|restart}"
}

multiple() {
    total=$#
    action=${!total}
    for (( i=1; i<total; i++)); do
        cd ${cwd}
        module=${!i}
        main ${module} ${2}
    done
}

case "$1" in
    all)
        all $@
        ;;
    usage)
        usage
        ;;
    *)
        multiple $@
        ;;
esac

cd ${cwd}
echo "=================="
echo "process finished"