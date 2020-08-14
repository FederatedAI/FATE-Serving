package cmd

import (
	"fate-serving-client/common"
	"fate-serving-client/pb"
	"fate-serving-client/rpc"
	"fmt"
	"os"
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
	/* featureData := make(map[string]interface{})
	sendToRemoteFeatureData := make(map[string]interface{})
	featureData["x0"] = 0.100016
	featureData["x1"] = 1.210
	featureData["x2"] = 2.321
	featureData["x3"] = 3.432
	featureData["x4"] = 4.543
	featureData["x5"] = 5.654
	featureData["x6"] = 5.654
	featureData["x7"] = 0.102345

	sendToRemoteFeatureData["device_id"] = "8"

	InferenceReq := InferenceRequest{"lr-test", featureData, sendToRemoteFeatureData}

	b, err := json.Marshal(InferenceReq)
	if err != nil {
		fmt.Println("Umarshal failed:", err)
		return
	}*/

	// 参数{0}直接传body字符串
	// inference {"serviceId":"lr-test","featureData":{"x0":0.100016,"x1":1.21,"x2":2.321,"x3":3.432,"x4":4.543,"x5":5.654,"x6":5.654,"x7":0.102345},"sendToRemoteFeatureData":{"device_id":"8"}}
	if len(cmd.Param) == 0 {
		fmt.Println("params empty")
		return
	}

	b := []byte(cmd.Param[0])

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
