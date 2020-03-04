########################################################
# Copyright 2019-2020 program was created VMware, Inc. #
# SPDX-License-Identifier: Apache-2.0                  #
########################################################

#!/bin/bash
set -e

BASEDIR=$(dirname "$0")
cd $BASEDIR
WORKINGDIR=`pwd`
source_code_dir=$(cd `dirname ${WORKINGDIR}`; pwd)
version=1.2.0

package() {
  docker run --rm -u $(id -u):$(id -g) -v ${source_code_dir}:/data/projects/fate/FATE-Serving --entrypoint="" maven:3.6-jdk-8 /bin/bash -c "cd /data/projects/fate/FATE-Serving && mvn clean package -DskipTests"
}

buildModule() {
  source .env
  for module in "serving-proxy" "serving-server"
  do
      echo "### START BUILDING ${module} ###"
      docker build --build-arg version=${version} --build-arg PREFIX=${PREFIX} -t ${PREFIX}/${module}:${TAG} -f ${source_code_dir}/docker-build/${module}/Dockerfile ${source_code_dir}
      echo "### FINISH BUILDING ${module} ###"
      echo ""
  done;
}

pushImage() {
  ## push image
  source .env
  for module in "serving-proxy" "serving-server"
  do
      echo "### START PUSH ${module} ###"
      docker push ${PREFIX}/${module}:${TAG}
      echo "### FINISH PUSH ${module} ###"
      echo ""
  done;
}

while [ "$1" != "" ]; do
    case $1 in
         modules)
                 buildModule
                 ;;
         package)
                 package
                 ;;
         all)
                 package
                 buildModule
                 ;;
         push)
                pushImage
                ;;
    esac
    shift
done