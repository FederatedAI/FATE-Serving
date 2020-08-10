package common

import (
	"encoding/json"
	"fmt"
)

func JsonToMap(jsonStr string) map[string]interface{} {
	//	jsonStr := `{"name": "jqw","age": 18}`
	var mapResult map[string]interface{}
	err := json.Unmarshal([]byte(jsonStr), &mapResult)
	if err != nil {
		fmt.Println("JsonToMapDemo err: ", err)
	}
	return mapResult
	//fmt.Println(mapResult)
}
