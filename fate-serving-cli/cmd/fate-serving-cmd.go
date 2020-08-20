/*
 * Copyright 2019 The FATE Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cmd

import (
	"encoding/json"
	"errors"
	"fate-serving-client/common"
	"fate-serving-client/pb"
	"fate-serving-client/rpc"
	"fmt"
	"os"
	"strings"
)

type Cmd struct {
	Name    string
	Param   []string
	Address string
}

type ShowModelCmd struct {
	Cmd
}

type HelpCmd struct {
	Cmd
}
type QuitCmd struct {
	Cmd
}

type ShowConfigCmd struct {
	Cmd
}

type InferenceCmd struct {
	Cmd
}

type BatchInferenceCmd struct {
	Cmd
}

func (cmd *QuitCmd) Run() {
	os.Exit(0)
}

type Run interface {
	Run()
}

func (cmd *ShowModelCmd) Run() {
	request := pb.QueryModelRequest{
		QueryType: 0,
	}
	queryModelResponse, error := rpc.QueryModelInfo(cmd.Address, &request)

	if error != nil {
		fmt.Println(error)
		return
	}
	if queryModelResponse != nil {
		fmt.Println("=========================================")
		for i := 0; i < len(queryModelResponse.ModelInfos); i++ {
			modelInfo := queryModelResponse.ModelInfos[i]
			tableName := modelInfo.TableName
			namespace := modelInfo.Namespace
			fmt.Println("tableName:	", tableName)
			fmt.Println("namesapce:	", namespace)
			//fmt.Println("content: ", modelInfo.Content)
			contentMap := common.JsonToMap(modelInfo.Content)
			serviceIds := modelInfo.ServiceIds
			fmt.Println("resourceName:	", contentMap["resourceName"])
			timestamp := contentMap["timestamp"]
			fmt.Println("timestamp:	", int(timestamp.(float64)))
			fmt.Println("role:		", contentMap["role"])
			fmt.Println("partId:		", contentMap["partId"])
			federationModelMap, err := json.Marshal(contentMap["federationModelMap"])
			if err == nil {
				fmt.Println("remoteInfo:	", string(federationModelMap))
			}

			rolePartyMapList, err := json.Marshal(contentMap["rolePartyMapList"])
			if err == nil {
				fmt.Println("roleParty:	", string(rolePartyMapList))
			}

			if serviceIds != nil {
				var ids string
				for i := 0; i < len(serviceIds); i++ {
					ids = ids + serviceIds[i] + ""
				}
				fmt.Println("serviceId:	", ids)
			}
			fmt.Println("=========================================")
		}
	}
}

func (cmd *HelpCmd) Run() {
	if len(cmd.Param) == 0 {
		fmt.Println("Usage:")
		fmt.Println("	./fate-serving-client [-h host] [-p port]")
		fmt.Println("The commands are:")
		fmt.Println("	showmodel		-- list the models in mermory")
		fmt.Println("	showconfig		-- list the configs in use")
		fmt.Println("	inference		-- single inference request, e.g. inference request.json")
		fmt.Println("	batchInference		-- batch inference request, e.g. batchInference request.json")
		fmt.Println("	help       		-- show all cmd")
		fmt.Println("	quit       		-- quit")
		fmt.Println("Use \"help <command>\" for more information about inference command.\n")
	} else {
		command := cmd.Param[0]
		switch {
		case command == "inference":
			fmt.Println("Usage: inference [args...]")
			fmt.Println("args:")
			fmt.Println("	{0}: request the absolute path of the json file")
			fmt.Println("file content format:")
			fmt.Println("	{\"serviceId\":\"lr-test\",\"featureData\":{\"x0\":0.100016,\"x1\":1.21,\"x2\":2.321,\"x3\":3.432,\"x4\":4.543,\"x5\":5.654,\"x6\":5.654,\"x7\":0.102345},\"sendToRemoteFeatureData\":{\"device_id\":\"8\"}}\n")
		case command == "batchInference":
			fmt.Println("Usage: batchInference [args...]")
			fmt.Println("args:")
			fmt.Println("	{0}: request the absolute path of the json file")
			fmt.Println("file content format:")
			fmt.Println("	{\"serviceId\":\"lr-test\",\"batchDataList\":[{\"index\":0,\"featureData\":{\"x0\":0.4853,\"x1\":1.1996,\"x2\":-1.574,\"x3\":-0.8811,\"x4\":-0.6176,\"x5\":0.5997,\"x6\":-0.5361,\"x7\":-0.1189,\"x8\":-1.5728},\"sendToRemoteFeatureData\":{\"device_id\":\"299\",\"phone_num\":585}}]}\n")
		default:

		}
	}
}

func (cmd *ShowConfigCmd) Run() {
	request := pb.QueryPropsRequest{}
	queryConfigResponse, error := rpc.QueryServingServerConfig(cmd.Address, &request)
	if error != nil {
		fmt.Println(error)
	}
	if queryConfigResponse != nil {
		dataString := string(queryConfigResponse.GetData())
		dataMap := common.JsonToMap(dataString)
		if error != nil {
			fmt.Println("erro", dataString)
		} else {
			for k, v := range dataMap {
				fmt.Println(k, "=", v)
			}
		}

	}
}

func (cmd *Cmd) Run() {

}

type InferenceRequest struct {
	ServiceId               string                 `json:"serviceId"`
	FeatureData             map[string]interface{} `json:"featureData"`
	SendToRemoteFeatureData map[string]interface{} `json:"sendToRemoteFeatureData"`
}

func (cmd *InferenceCmd) Run() {
	// inference /data/projects/request.json
	// {"serviceId":"lr-test","featureData":{"x0":0.100016,"x1":1.21,"x2":2.321,"x3":3.432,"x4":4.543,"x5":5.654,"x6":5.654,"x7":0.102345},"sendToRemoteFeatureData":{"device_id":"8"}}
	var err error
	if len(cmd.Param) == 0 {
		fmt.Println("params empty")
		return
	}

	content, err := readParams(cmd.Param[0])
	if err != nil {
		fmt.Println(err)
		return
	}

	inferenceMsg := pb.InferenceMessage{
		Body: content,
	}

	inferenceResp, err := rpc.Inference(cmd.Address, &inferenceMsg)
	if err != nil {
		fmt.Println(err)
		return
	}
	if inferenceResp != nil {
		resp := string(inferenceResp.GetBody())
		fmt.Println(resp)
	}
}

func (cmd *BatchInferenceCmd) Run() {
	/*{
		"serviceId": "lr-test",
		"batchDataList": [
			{
				"index": 0,
				"featureData": {
					"x0": 0.4853,
					"x1": 1.1996,
					"x2": -1.574,
					"x3": -0.8811,
					"x4": -0.6176,
					"x5": 0.5997,
					"x6": -0.5361,
					"x7": -0.1189,
					"x8": -1.5728
				},
				"sendToRemoteFeatureData": {
					"device_id": "299",
					"phone_num": 585
				}
			}
		]
	}*/
	var err error
	if len(cmd.Param) == 0 {
		fmt.Println("params empty")
		return
	}

	// params file path
	content, err := readParams(cmd.Param[0])
	if err != nil {
		fmt.Println(err)
		return
	}

	inferenceMsg := pb.InferenceMessage{
		Body: content,
	}

	inferenceResp, err := rpc.BatchInference(cmd.Address, &inferenceMsg)
	if err != nil {
		fmt.Println(err)
		return
	}
	if inferenceResp != nil {
		resp := string(inferenceResp.GetBody())
		fmt.Println(resp)
	}
}

func readParams(path string) ([]byte, error) {
	// read file content
	file, err := os.Open(path)

	if err != nil {
		return nil, err
	}

	stat, _ := file.Stat()
	if size := stat.Size(); size == 0 {
		return nil, errors.New("file content empty")
	}

	buffer := make([]byte, stat.Size())
	_, err = file.Read(buffer)
	if err != nil {
		return nil, err
	}

	request := string(buffer)
	request = strings.ReplaceAll(request, "\n", "")
	request = strings.ReplaceAll(request, "\r", "")
	request = strings.ReplaceAll(request, "\t", "")
	return []byte(request), err
}
