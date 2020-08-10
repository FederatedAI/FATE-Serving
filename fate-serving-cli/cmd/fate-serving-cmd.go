package cmd

import (
	"fate-serving-client/common"
	"os"
	"fate-serving-client/pb"
	"fmt"
	"fate-serving-client/rpc"
)

type Cmd struct {
	Name    string
	Param   []string
	Address string;
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

func (cmd *QuitCmd) Run() {
	os.Exit(1);
}

type Run interface {
	Run()
}

func (cmd *ShowModelCmd) Run() {
	request := pb.QueryModelRequest{
		QueryType: 1,
	}
	queryModelResponse, error := rpc.QueryModelInfo(cmd.Address, &request)

	if (error != nil) {
		fmt.Println(error)
		return
	}
	if (queryModelResponse != nil) {
		fmt.Println("=========================================")
		for i := 0; i < len(queryModelResponse.ModelInfos); i++ {
			modelInfo := queryModelResponse.ModelInfos[i];
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
			if (serviceIds != nil) {
				fmt.Printf("serviceIds: ")
				for i := 0; i < len(serviceIds); i++ {
					fmt.Printf(serviceIds[i] + " ")
				}
				fmt.Println();
			}
			fmt.Println("=========================================")
		}
	}
}

func (cmd *HelpCmd) Run() {

	fmt.Println("surport cmd:")
	fmt.Println("showmodel  -- list the models in mermory")
	fmt.Println("showconfig -- list the configs in use")
	fmt.Println("help       -- show all cmd")
	fmt.Println("quit       -- quit")
}

func (cmd *ShowConfigCmd) Run() {
	request := pb.QueryPropsRequest{

	}
	queryConfigResponse, error := rpc.QueryServingServerConfig(cmd.Address, &request)
	if (error != nil) {
		fmt.Println(error)
	}
	if (queryConfigResponse != nil) {
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
