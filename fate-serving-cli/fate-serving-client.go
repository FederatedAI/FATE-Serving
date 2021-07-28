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

package main

import (
	"encoding/json"
	"fate-serving-client/cmd"
	"fate-serving-client/common"
	"fate-serving-client/pb"
	"fate-serving-client/rpc"
	"sort"
	"strings"
	"time"

	"fmt"

	"github.com/desertbit/grumble"
	"github.com/fatih/color"
	"github.com/samuel/go-zookeeper/zk"

	"strconv"
)

// 实际中应该用更好的变量名
var (
	h             bool
	host          string
	port          int
	v, V          bool
	t, T          bool
	q             *bool
	s             string
	p             string
	c             string
	g             string
	address       string
	INFERENCETEST = `{
    "serviceId": "",
    "batchDataList": [
        {
            "featureData": {
                "x0": 1.88669,
                "x1": -1.359293,
                "x2": 2.303601,
                "x3": 2.00137,
                "x4": 1.307686
            },
            "sendToRemoteFeatureData": {
                "device_id": "aaaaa",
                "phone_num": "122222222"
            }
        },
        {
            "featureData": {
                "x0": 1.88669,
                "x1": -1.359293,
                "x2": 2.303601,
                "x3": 2.00137,
                "x4": 1.307686
            },
            "sendToRemoteFeatureData": {
                "device_id": "aaaaa",
                "phone_num": "122222222"
            }
        }
    ]
}`
	BATCHINFERENCETEST = `{
		"serviceId": "",
		"batchDataList": [
			{
				"featureData": {
					"x0": 1.88669,
					"x1": -1.359293,
					"x2": 2.303601,
					"x3": 2.00137,
					"x4": 1.307686
				},
				"sendToRemoteFeatureData": {
					"device_id": "aaaaa",
					"phone_num": "122222222"
				}
			},
			{
				"featureData": {
					"x0": 1.88669,
					"x1": -1.359293,
					"x2": 2.303601,
					"x3": 2.00137,
					"x4": 1.307686
				},
				"sendToRemoteFeatureData": {
					"device_id": "aaaaa",
					"phone_num": "122222222"
				}
			}
		]
	}`
)

type Err struct {
	Code int
	Msg  string
}

func (e *Err) Error() string {
	err, _ := json.Marshal(e)
	return string(err)
}

var defaultAddress = "localhost:8000"

func getAddress(c *grumble.Context) string {
	address := c.Flags.String("address")
	if len(address) == 0 {
		address = defaultAddress
	}
	keyColor.Println(address)
	return address
}

var keyLength = 20

func autoFillString(content string, fillData string, lenght int) string {
	size := len(content)
	if size < lenght {
		for i := 0; i < lenght-size; i++ {
			content = content + fillData
		}

	}
	return content
}

func autuFillKey(content string) string {
	return autoFillString(content, "", keyLength) + ":   "
}

var contentColor *color.Color
var keyColor *color.Color
var valueColor *color.Color
var lineColor *color.Color
var gapColor *color.Color
var gapLine string
var COMPONENTPATH = "/FATE-COMPONENTS"
var SERVICESPATH = "/FATE-SERVICES"

func init() {
	contentColor = color.New(color.FgCyan)
	keyColor = color.New(color.FgHiMagenta)
	valueColor = color.New(color.FgGreen)
	gapColor = color.New(color.FgHiBlue)
	gapLine = autoFillString("", "=", 100)
	// flag.StringVar(&host, "h", "localhost", "host ip")
	// flag.IntVar(&port, "p", 8000, "port")
	//address = host + ":" + strconv.Itoa(port)
	// fmt.Println("default address " + defaultAddress)
	// fmt.Printf("\x1b[%dmhello world 30: 黑 \x1b[0m\n", 30)
	// fmt.Printf("\x1b[%dmhello world 31: 红 \x1b[0m\n", 31)
	// fmt.Printf("\x1b[%dmhello world 32: 绿 \x1b[0m\n", 32)
	// fmt.Printf("\x1b[%dmhello world 33: 黄 \x1b[0m\n", 33)
	// fmt.Printf("\x1b[%dmhello world 34: 蓝 \x1b[0m\n", 34)
	// fmt.Printf("\x1b[%dmhello world 35: 紫 \x1b[0m\n", 35)
	// fmt.Printf("\x1b[%dmhello world 36: 深绿 \x1b[0m\n", 36)
	// fmt.Printf("\x1b[%dmhetableName
	// App.AddCommand(&grumble.Command{
	// 	Name:    "daemon",
	// 	Help:    "run the daemon",
	// 	Aliases: []string{"run"},
	// 	Flags: func(f *grumble.Flags) {
	// 		f.Duration("t", "timeout", time.Second, "timeout duration")
	// 	},
	// 	Args: func(a *grumble.Args) {
	// 		a.Bool("production", "whether to start the daemon in production or development mode")
	// 		a.Int("opt-level", "the optimization mode", grumble.Default(3))
	// 		a.StringList("services", "additional services that should be started", grumble.Default([]string{"test", "te11"}))
	// 	},
	// 	Run: func(c *grumble.Context) error {
	// 		c.App.Println("timeout:", c.Fla	fmt.Printf("\x1b[%dmhello world 31: 红 \x1b[0m\n", 31)
	// 	},
	// })

	cluster := &grumble.Command{
		Name:     "zk",
		Help:     "zk",
		LongHelp: "zk",
		Flags: func(f *grumble.Flags) {
			f.String("a", "address", "localhost:2181", "address of zookeeper")
			f.String("m", "mod", "c", "c : show compnents in this cluster  , s : show services in this cluster, default c")

		},
		Run: func(c *grumble.Context) error {
			var hosts = []string{getAddress(c)}
			conn, _, err := zk.Connect(hosts, time.Second*5)
			defer conn.Close()
			if err != nil {
				return nil
			}
			mod := c.Flags.String("mod")
			switch mod {
			case "c":
				printZkComponents(conn)
				printZkService(conn)
				break
			case "s":
				break

			}

			return nil
		},
	}

	showflowCommand := &grumble.Command{
		Name:     "flow",
		Help:     "flow",
		LongHelp: "flow",
		Flags: func(f *grumble.Flags) {
			f.String("a", "address", "localhost:8000", "ip:port")
			f.Int64("s", "seconds", 5, "seconds")
			f.Int64("c", "count", 10, "count")

		},
		Run: func(c *grumble.Context) error {
			cache := make(map[string]interface{})
			request := pb.QueryMetricRequest{}
			address := getAddress(c)
			seconds := c.Flags.Int64("seconds")
			countNum := c.Flags.Int64("count")
			//countNum, _ := strconv.ParseInt(countString, 0, 64)
			i := int64(0)
			for ; i < countNum; i++ {

				now := time.Now().UnixNano() / 1e6
				request.BeginMs = now - 1000*seconds
				queryConfigResponse, error := rpc.QueryMetric(address, &request)

				if error != nil {
					return error
				}
				if queryConfigResponse != nil {
					dataString := string(queryConfigResponse.GetData())
					data := common.JsonToList(dataString)
					for _, contentMap := range data {
						if contentMap["passQps"].(float64) > 0 {
							//tm := time.Unix(int64(contentMap["timestamp"].(float64)/1000), 0)
							timestamp := contentMap["timestamp"].(float64)
							timestampString := strconv.FormatFloat(timestamp, 'f', 0, 64)
							resource := contentMap["resource"].(string)
							key := timestampString + resource
							if cache[key] == nil {
								fmt.Println(timestampString, resource, contentMap["passQps"].(float64), contentMap["successQps"].(float64))
								cache[key] = 0
							}
						}

					}

					time.Sleep(time.Duration(seconds) * time.Second)

				}
			}
			return nil
		},
	}

	uncaryCall := &grumble.Command{
		Name:     "unarycall",
		Help:     "unarycall",
		LongHelp: "unarycall",
		Flags: func(f *grumble.Flags) {
			f.String("a", "address", "localhost:8000", "the address of serving-server")
			f.String("f", "filepath", "", "the file path of json file")
			f.String("s", "serviceId", "", "the serviceId of model")

		},
		Run: func(c *grumble.Context) error {
			c.App.Config().PromptColor.Add()
			var err error
			address := c.Flags.String("address")
			filePath := c.Flags.String("filepath")
			if len(filePath) != 0 {
				stringContext, _ := cmd.ReadFile(filePath)
				if err != nil {
					fmt.Println(err)
					return nil
				}

				request := pb.Packet{}
				common.JsonToStuct(stringContext, &request)
				fmt.Println(request)
				response, err := rpc.UnaryCall(address, &request)

				if err != nil {
					fmt.Println("Connection error")
					return nil
				}
				if response != nil {

					fmt.Println(response)
				}
			} else {
				fmt.Println("please use -f ")
			}

			return nil
		},
	}

	showJvmCommand := &grumble.Command{
		Name:     "jvm",
		Help:     "jvm",
		LongHelp: "jvm",
		Flags: func(f *grumble.Flags) {
			f.String("a", "address", "localhost:8000", "ip:port")
			f.Int64("s", "seconds", 5, "seconds")
			f.Int64("c", "count", 1, "count")

		},
		Run: func(c *grumble.Context) error {

			cache := make(map[float64]interface{})
			sortList := make([]float64, 0, 0)

			request := pb.QueryJvmInfoRequest{}
			address := getAddress(c)
			seconds := c.Flags.Int64("seconds")
			countNum := c.Flags.Int64("count")
			i := int64(0)
			for ; i < countNum; i++ {

				queryJvmResponse, error := rpc.QueryJvmInfo(address, &request)

				if error != nil {
					return error
				}
				if queryJvmResponse != nil {
					dataString := string(queryJvmResponse.GetData())
					data := common.JsonToList(dataString)
					for _, contentMap := range data {
						timestamp := contentMap["timestamp"].(float64)

						if cache[timestamp] == nil {
							cache[timestamp] = contentMap
							sortList = append(sortList, timestamp)
						}

					}

					sort.Float64s(sortList)
					for _, timestamp := range sortList {
						printJvmInfo(cache[timestamp].(map[string]interface{}))
					}
					if i < countNum-1 {
						time.Sleep(time.Duration(seconds) * time.Second)
					}

				}
			}

			return nil
		},
	}

	showConfigCommand := &grumble.Command{
		Name:     "showconfig",
		Help:     "showconfig",
		LongHelp: "show the config",
		Flags: func(f *grumble.Flags) {
			f.String("a", "address", "localhost:8000", "ip:port")
		},
		Run: func(c *grumble.Context) error {
			request := pb.QueryPropsRequest{}

			address := getAddress(c)
			queryConfigResponse, error := rpc.QueryServingServerConfig(address, &request)

			if error != nil {
				return error
			}
			if queryConfigResponse != nil {
				dataString := string(queryConfigResponse.GetData())
				dataMap := common.JsonToMap(dataString)
				if error != nil {
					c.App.Println("error", dataString)
				} else {
					for k, v := range dataMap {
						keyColor.Printf(k)
						c.App.Println("=", v)
					}
				}

			}
			return nil
		},
	}

	showModelCommand := &grumble.Command{
		Name:     "showmodel",
		Help:     "showmodel",
		LongHelp: "show the model in the selected serving-server instance",
		Flags: func(f *grumble.Flags) {
			f.String("a", "address", "localhost:8000", "the address of serving-server")
			f.String("d", "detail", "no", "show details in model")
			//f.String("h", "host", "localhost", "the ip of serving-server")

			// flag.StringVar(&host, "h", "localhost", "host ip")
			// flag.IntVar(&port, "p", 8000, "port")
		},
		Run: func(c *grumble.Context) error {
			c.App.Config().PromptColor.Add()
			request := pb.QueryModelRequest{
				QueryType: 0,
			}
			address := getAddress(c)
			//	detail := c.Flags.String("detail")

			queryModelResponse, error := rpc.QueryModelInfo(address, &request)

			if error != nil {
				c.App.PrintError(error)
				return error
			}
			c.App.Println("try to query model info from " + address)
			if queryModelResponse != nil {
				if queryModelResponse.GetRetcode() == 0 {
					c.App.Println("success")
				}

				gapColor.Println(gapLine)
				blank := autoFillString("", " ", 20)

				for i := 0; i < len(queryModelResponse.ModelInfos); i++ {
					modelInfo := queryModelResponse.ModelInfos[i]

					// tableName := modelInfo.TableName
					// namespace := modelInfo.Namespace

					keyColor.Printf(autuFillKey("index"))
					valueColor.Println(i + 1)
					contentMap := common.JsonToMap(modelInfo.Content)
					//if detail == "no" {
					delete(contentMap, "components")
					delete(contentMap, "modelProcessor")

					//}
					keyColor.Printf(autuFillKey("content"))
					contentString, _ := json.MarshalIndent(contentMap, blank, "\t")
					//	if err != nil {
					valueColor.Println(string(contentString))
					//	}

					// keyColor.Printf(autuFillKey("tableName"))
					// valueColor.Println(tableName)
					// keyColor.Printf(autuFillKey("namesapce"))
					// valueColor.Println(namespace)

					//	fmt.Println("content: ", modelInfo.Content)

					// serviceIds := modelInfo.ServiceIds1
					// keyColor.Printf(autuFillKey("resourceName"))
					// valueColor.Println(contentMap["resourceName"])
					// timestamp := contentMap["timestamp"]
					// keyColor.Printf(autuFillKey("timestamp"))
					// valueColor.Println(int(timestamp.(float64)))
					// keyColor.Printf(autuFillKey("role"))
					// valueColor.Println(contentMap["role"])
					// keyColor.Printf(autuFillKey("partId"))
					// valueColor.Println(contentMap["partId"])

					// federationModelMap, err := json.MarshalIndent(contentMap["federationModelMap"], blank, "\t")
					// if err == nil {
					// 	keyColor.Printf(autuFillKey("remoteInfo"))
					// 	valueColor.Println(string(federationModelMap))
					// }
					// rolePartyMapList, err := json.MarshalIndent(contentMap["rolePartyMapList"], blank, "\t")
					// if err == nil {
					// 	keyColor.Printf(autuFillKey("roleParty"))
					// 	valueColor.Println(string(rolePartyMapList))
					// }

					// if serviceIds != nil {
					// 	var ids string
					// 	for i := 0; i < len(serviceIds); i++ {
					// 		ids = ids + serviceIds[i] + ""
					// 	}
					// 	keyColor.Printf(autuFillKey("serviceId"))
					// 	valueColor.Println(ids)
					// }
					gapColor.Println(gapLine)
				}
			}

			//c.App.Println(c.Flags.String("directory"))
			return nil
		},
	}

	showHealthInfoCommand := &grumble.Command{
		Name:     "showHealthInfo",
		Help:     "showHealthInfo",
		LongHelp: "showHealthInfo",
		Flags: func(f *grumble.Flags) {
			f.String("a", "address", "localhost:8000", "ip:port")
			f.Int64("s", "seconds", 5, "seconds")
			f.Int64("c", "count", 1, "count")
		},
		Run: func(c *grumble.Context) error {
			cache := make(map[float64]interface{})
			sortList := make([]float64, 0, 0)
			request := pb.HealthCheckRequest{}
			address := getAddress(c)
			seconds := c.Flags.Int64("seconds")
			countNum := c.Flags.Int64("count")
			i := int64(0)
			for ; i < countNum; i++ {
				queryHealthInfoResponse, error := rpc.QueryHealthInfo(address, &request)
				if error != nil {
					return error
				}
				if queryHealthInfoResponse != nil {
					dataString := string(queryHealthInfoResponse.GetData())
					data := common.JsonToList(dataString)
					for _, contentMap := range data {
						timestamp := contentMap["timestamp"].(float64)
						if cache[timestamp] == nil {
							cache[timestamp] = contentMap
							sortList = append(sortList, timestamp)
						}
					}
					sort.Float64s(sortList)
					for _, timestamp := range sortList {
						printHealthInfo(cache[timestamp].(map[string]interface{}))
					}
					for i < countNum-1 {
						time.Sleep(time.Duration(seconds) * time.Second)
					}
				}
			}
			return nil
		},
	}

	inferenceCommand := &grumble.Command{
		Name:     "inference",
		Help:     "inference",
		LongHelp: "inference",
		Flags: func(f *grumble.Flags) {
			f.String("a", "address", "localhost:8000", "the address of serving-server")
			f.String("f", "filepath", "", "the file path of json file")
			f.String("s", "serviceId", "", "the serviceId of model")
			//f.String("h", "host", "localhost", "the ip of serving-server")

			// flag.StringVar(&host, "h", "localhost", "host ip")
			// flag.IntVar(&port, "p", 8000, "port")
		},
		Run: func(c *grumble.Context) error {
			c.App.Config().PromptColor.Add()
			var err error
			address := c.Flags.String("address")
			filePath := c.Flags.String("filepath")

			var content []byte
			if len(filePath) == 0 {
				servieId := c.Flags.String("serviceId")
				if len(servieId) == 0 {
					fmt.Println("serviceId for default testing data is expected")
					return nil
				} else {
					position := strings.IndexAny(INFERENCETEST, ":")
					position += 3
					content = []byte(INFERENCETEST[0:position] + servieId + INFERENCETEST[position:])
				}
			} else {
				content, err = cmd.ReadParams(filePath)
				if err != nil {
					fmt.Println(err)
					return nil
				}
			}
			inferenceMsg := pb.InferenceMessage{
				Body: content,
			}

			inferenceResp, err := rpc.Inference(address, &inferenceMsg)
			if err != nil {
				fmt.Println("Connection error")
				return nil
			}
			if inferenceResp != nil {
				resp := string(inferenceResp.GetBody())
				fmt.Println(resp)
			}
			//c.App.Println(c.Flags.String("directory"))
			return nil
		},
	}

	batchInferenceCommand := &grumble.Command{
		Name:     "batchInference",
		Help:     "batchInference",
		LongHelp: "batchInference",
		Flags: func(f *grumble.Flags) {
			f.String("a", "address", "localhost:8000", "serving-server的端口以及地址,如果不设置则默认使用localhost:8000")
			f.String("f", "filepath", "", "用于预测的json文件路径")
			f.String("s", "serviceId", "", "the serviceId of model")
			//f.String("h", "host", "localhost", "the ip of serving-server")

			// flag.StringVar(&host, "h", "localhost", "host ip")
			// flag.IntVar(&port, "p", 8000, "port")
		},
		Run: func(c *grumble.Context) error {
			c.App.Config().PromptColor.Add()
			var err error
			filePath := c.Flags.String("filepath")

			var content []byte
			address = c.Flags.String("address")
			if len(filePath) == 0 {
				servieId := c.Flags.String("serviceId")
				if len(servieId) == 0 {
					fmt.Println("serviceId for default testing data is expected")
					return nil
				} else {
					position := strings.IndexAny(BATCHINFERENCETEST, ":")
					position += 3
					content = []byte(BATCHINFERENCETEST[0:position] + servieId + BATCHINFERENCETEST[position:])
				}
			} else {
				content, err = cmd.ReadParams(filePath)
				if err != nil {
					fmt.Println(err)
					return nil
				}
			}

			inferenceMsg := pb.InferenceMessage{
				Body: content,
			}

			inferenceResp, err := rpc.BatchInference(address, &inferenceMsg)
			if err != nil {
				fmt.Println("Connection error")
				return nil
			}
			if inferenceResp != nil {
				resp := string(inferenceResp.GetBody())
				fmt.Println(resp)
			}
			//c.App.Println(c.Flags.String("directory"))
			return nil
		},
	}

	App.AddCommand(showModelCommand)
	App.AddCommand(showConfigCommand)
	App.AddCommand(inferenceCommand)
	App.AddCommand(batchInferenceCommand)
	App.AddCommand(showflowCommand)
	App.AddCommand(cluster)
	App.AddCommand(showJvmCommand)
	App.AddCommand(uncaryCall)
	App.AddCommand(showHealthInfoCommand)

	// adminCommand.AddCommand(&grumble.Command{
	// 	Name: "root",
	// 	Help: "root the machine",
	// 	Run: func(c *grumble.Context) error {
	// 		c.App.Println(c.Flags.String("directory"))
	// 		return errors.New("failed")
	// 	},
	// })
}

func main() {
	// flag.StringVar(&host, "h", "localhost", "host ip")
	// flag.IntVar(&port, "p", 8000, "port")
	address = host + ":" + strconv.Itoa(port)
	grumble.Main(App)
}

func printConfig() {
	fmt.Println("use default address : " + address)

}

var App = grumble.New(&grumble.Config{
	Name:        "fate-serving-client",
	Description: "An awesome client",
	Flags: func(f *grumble.Flags) {
		f.String("a", "address", "localhost", "the ip of serving-server")
		printConfig()
		//flag.StringVar(&host, "h", "localhost", "host ip")
		// flag.IntVar(&port, "p", 8000, "port")
	},
})

func printJvmInfo(contentMap map[string]interface{}) {

	timestamp := contentMap["timestamp"].(float64)
	threadCount := contentMap["threadCount"].(float64)
	timestampString := strconv.FormatFloat(timestamp, 'f', 0, 64)
	yongGcCount := contentMap["yongGcCount"].(float64)
	fullGcCount := contentMap["fullGcCount"].(float64)
	heapMap := contentMap["heap"].(map[string]interface{})
	yongGcContent := "yongGcCount:" + strconv.FormatFloat(yongGcCount, 'f', 0, 64)
	fullGcContent := "fullGcCount:" + strconv.FormatFloat(fullGcCount, 'f', 0, 64)
	heapMaxContent := "heap max size:" + strconv.FormatFloat(heapMap["max"].(float64), 'f', 0, 64)
	heapUsedContent := "heap used size:" + strconv.FormatFloat(heapMap["used"].(float64), 'f', 0, 64)
	usedPercentContent := "used percent:" + strconv.FormatFloat(heapMap["usedPercent"].(float64), 'f', 0, 64)
	threadCountContent := "threadCount:" + strconv.FormatFloat(threadCount, 'f', 0, 64)
	fmt.Println(timestampString, yongGcContent, fullGcContent, heapMaxContent,
		heapUsedContent, usedPercentContent, threadCountContent)

}

func printHealthInfo(contentMap map[string]interface{}) {
	timestamp := contentMap["timestamp"].(float64)
	timestampString := strconv.FormatFloat(timestamp, 'f', 0, 64)
	proxy := contentMap["proxy"].(map[string]interface{})
	pOkList := proxy["okList"].([]map[string]string)
	pWarnList := proxy["warnList"].([]map[string]string)
	pErrorList := proxy["errorList"].([]map[string]string)
	serving := contentMap["serving"].(map[string]interface{})
	sOkList := serving["okList"].([]map[string]string)
	sWarnList := serving["warnList"].([]map[string]string)
	sErrorList := serving["errorList"].([]map[string]string)
	pOkList = append(pOkList, sOkList...)
	pWarnList = append(pWarnList, sWarnList...)
	pErrorList = append(pErrorList, sErrorList...)
	fmt.Printf("%s\n", timestampString)
	if len(pOkList) != 0 {
		fmt.Println("----------------------------------------------------------")
		for _, ok := range pOkList {
			fmt.Printf("checkItemName:%s, msg:%s;", ok["checkItemName"], ok["msg"])
		}
		fmt.Println("----------------------------------------------------------")
	}
	if len(pWarnList) != 0 {
		fmt.Println("----------------------------------------------------------")
		for _, ok := range pWarnList {
			fmt.Printf("checkItemName:%s, msg:%s;", ok["checkItemName"], ok["msg"])
		}
		fmt.Println("----------------------------------------------------------")
	}
	if len(pErrorList) != 0 {
		fmt.Println("----------------------------------------------------------")
		for _, ok := range pErrorList {
			fmt.Printf("checkItemName:%s, msg:%s;", ok["checkItemName"], ok["msg"])
		}
		fmt.Println("----------------------------------------------------------")
	}
}

func printZkService(conn *zk.Conn) error {
	children, _, err := conn.Children(SERVICESPATH)
	if err != nil {
		return err
	}
	gapColor.Println(gapLine)
	fmt.Println("Registered Services")
	for _, componentLevel := range children {
		//fmt.Println("test node: ", p)
		//geti
		tmp := SERVICESPATH + "/" + componentLevel
		//fmt.Println("cur node: ", tmp)
		environments, _, err := conn.Children(tmp)
		if err != nil {
			continue
		}
		for _, environmentLevel := range environments {
			valueColor.Println("Environmrnt: " + "service " + "host")
			serviceLevelPath := tmp + "/" + environmentLevel
			services, _, err := conn.Children(serviceLevelPath)
			if err != nil {
				continue
			}
			for _, serviceLevel := range services {
				hosts, _, err := conn.Children(serviceLevelPath + "/" + serviceLevel + "/" + "providers")
				if err != nil {
					continue
				}
				for _, hostLevel := range hosts {
					hostLevel = hostLevel[strings.IndexAny(hostLevel, "1"):]
					hostLevel = hostLevel[0 : strings.IndexAny(hostLevel, "F")-2]
					hostLevel = strings.Replace(hostLevel, "%3A", ":", 1)
					keyColor.Println(environmentLevel + " " + serviceLevel + " " + hostLevel)
				}
			}
		}

	}
	gapColor.Println(gapLine)
	return nil
}

func printZkComponents(conn *zk.Conn) error {

	children, _, err := conn.Children(COMPONENTPATH)
	if err != nil {
		return err
	}
	gapColor.Println(gapLine)
	fmt.Println("Registered Components:")
	for _, childlevel1 := range children {
		//fmt.Println("test node: ", p)
		//geti
		tmp := COMPONENTPATH + "/" + childlevel1
		//fmt.Println("cur node: ", tmp)
		childlevel2, _, err := conn.Children(tmp)
		if err != nil {
			continue
		}
		keyColor.Printf(transformComponent(childlevel1) + " = ")
		valueColor.Println(childlevel2)
	}
	gapColor.Println(gapLine)
	return nil
}

func transformComponent(ori string) string {
	switch ori {
	case "serving":
		return "serving-server"
	case "proxy":
		return "serving-proxy"
	default:
		return ori
	}
}
func printUsage() {

}
