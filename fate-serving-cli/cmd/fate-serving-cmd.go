package cmd

import (
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
	os.Exit(1)
}

type Run interface {
	Run()
}

func (cmd *ShowModelCmd) Run() {
	request := pb.QueryModelRequest{
		QueryType: 1,
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
			fmt.Println("tableName: ", tableName)
			fmt.Println("namesapce: ", namespace)
			fmt.Println("content: ", modelInfo.Content)
			contentMap := common.JsonToMap(modelInfo.Content)
			serviceIds := modelInfo.ServiceIds
			fmt.Println("timestamp: ", contentMap["timestamp"])
			fmt.Println("role: ", contentMap["role"])
			fmt.Println("partId: ", contentMap["partId"])
			fmt.Println("remote info: ", contentMap["federationModelMap"])
			if serviceIds != nil {
				fmt.Printf("serviceIds: ")
				for i := 0; i < len(serviceIds); i++ {
					fmt.Printf(serviceIds[i] + " ")
				}
				fmt.Println()
			}
			fmt.Println("=========================================")
		}
	}
}

func (cmd *HelpCmd) Run() {

	fmt.Println("surport cmd:")
	fmt.Println("showmodel  -- list the models in mermory")
	fmt.Println("showconfig -- list the configs in use")
	fmt.Println("inference  -- single inference request, e.g. inference #{body_json}")
	fmt.Println("batchInference  -- batch inference request, e.g. inference #{body_json_file_path}")
	fmt.Println("help       -- show all cmd")
	fmt.Println("quit       -- quit")
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
	// 参数{0}直接传body字符串
	// inference {"serviceId":"lr-test","featureData":{"x0":0.100016,"x1":1.21,"x2":2.321,"x3":3.432,"x4":4.543,"x5":5.654,"x6":5.654,"x7":0.102345},"sendToRemoteFeatureData":{"device_id":"8"}}
	if len(cmd.Param) == 0 {
		fmt.Println("params empty")
		return
	}

	param := cmd.Param[0]
	param = strings.ReplaceAll(param, "\n", "")
	param = strings.ReplaceAll(param, "\r", "")
	param = strings.ReplaceAll(param, "\t", "")

	b := []byte(param)

	inferenceMsg := pb.InferenceMessage{
		Body: b,
	}

	inferenceResp, error := rpc.Inference(cmd.Address, &inferenceMsg)
	if error != nil {
		fmt.Println(error)
	}
	if inferenceResp != nil {
		dataString := string(inferenceResp.GetBody())
		resp := common.JsonToMap(dataString)
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
	path := cmd.Param[0]

	// read file content
	file, err := os.Open(path)

	if err != nil {
		fmt.Println("cannot open file %s", path)
		return
	}

	stat, _ := file.Stat()
	if size := stat.Size(); size == 0 {
		fmt.Println("file content empty")
		return
	}

	buffer := make([]byte, stat.Size())
	_, err = file.Read(buffer)
	if err != nil {
		fmt.Println("read failed:", err)
		return
	}

	request := string(buffer)
	request = strings.ReplaceAll(request, "\n", "")
	request = strings.ReplaceAll(request, "\r", "")
	request = strings.ReplaceAll(request, "\t", "")

	inferenceMsg := pb.InferenceMessage{
		Body: []byte(request),
	}

	inferenceResp, error := rpc.BatchInference(cmd.Address, &inferenceMsg)
	if error != nil {
		fmt.Println(error)
	}
	if inferenceResp != nil {
		dataString := string(inferenceResp.GetBody())
		resp := common.JsonToMap(dataString)
		fmt.Println(resp)
	}
}
