// package zkcli

// import (
// 	"fmt"
// 	"time"

// 	"github.com/samuel/go-zookeeper/zk"
// )

// func QueryZkInfo(host string) string {
// 	var hosts = []string{host}
// 	conn, _, err := zk.Connect(hosts, time.Second*5)
// 	defer conn.Close()
// 	if err != nil {
// 		fmt.Errorf(err)
// 		return ""
// 	}
// 	var path = "/consumers"

// 	// get
// 	/*_,_,err := conn.Get(path)
// 	  if err != nil {
// 	      fmt.Println(err)
// 	      return
// 	  }*/

// 	children, _, err := conn.Children(path)
// 	if err != nil {
// 		return
// 	}
// 	for _, p := range children {
// 		fmt.Println("test node: ", p)
// 		//geti
// 		tmp := path + "/" + p + "/offsets"
// 		fmt.Println("cur node: ", tmp)
// 		child, _, err := conn.Children(tmp)
// 		if err != nil {
// 			continue
// 		}
// 		for _, t := range child {
// 			fmt.Println("test child: ", t)
// 			if t == "topic1" || t == "topic2" {
// 				fmt.Println("wky : ", p)
// 			}
// 		}
// 	}

// 	// conn.
// }
