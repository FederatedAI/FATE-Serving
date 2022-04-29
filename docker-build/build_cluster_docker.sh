########################################################
# Copyright 2019-2020 program was created VMware, Inc. #
# SPDX-License-Identifier: Apache-2.0                  #
########################################################

#!/usr/bin/env bash
set -e

BASEDIR=$(dirname "$0")
cd $BASEDIR
WORKINGDIR=`pwd`

source_code_dir=$(cd `dirname ${WORKINGDIR}`; pwd)


if [ -z "$TAG" ]; then
        TAG=latest
fi
if [ -z "$PREFIX" ]; then
        PREFIX=federatedai
fi

version=$(git describe --tags)

source .env

echo "Docker build"
echo "Info:"
echo "  version: ${version}"
echo "  PREFIX: ${PREFIX}"
echo "  Tag: ${TAG}"


package() {
  docker run --rm -u $(id -u):$(id -g) -v ${source_code_dir}:/data/projects/fate/FATE-Serving --entrypoint="" maven:3.6-jdk-8 /bin/bash -c "cd /data/projects/fate/FATE-Serving && mvn clean package -DskipTests"
}

buildModule() {
  for module in "serving-proxy" "serving-server" "serving-admin"
  do
      echo "### START BUILDING ${module} ###"
      docker build --build-arg version=${version} -t ${PREFIX}/${module}:${TAG} -f ${source_code_dir}/docker-build/${module}/Dockerfile ${source_code_dir}
      echo "### FINISH BUILDING ${module} ###"
      echo ""
  done;
}

pushImage() {
  ## push image
  for module in "serving-proxy" "serving-server" "serving-admin"
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
