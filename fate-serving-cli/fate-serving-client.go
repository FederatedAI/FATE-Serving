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
	"bufio"
	"fate-serving-client/cmd"
	"fate-serving-client/rpc"
	"flag"
	"fmt"
	"os"
	"os/signal"
	"strings"
	"syscall"
	"time"

	"strconv"
)

// 实际中应该用更好的变量名
var (
	h       bool
	host    string
	port    int
	v, V    bool
	t, T    bool
	q       *bool
	s       string
	p       string
	c       string
	g       string
	address string
)

func init() {
	flag.StringVar(&host, "h", "localhost", "host ip")
	flag.IntVar(&port, "p", 8000, "port")
	address = host + ":" + strconv.Itoa(port)
}

func main() {
	flag.Parse()
	go func() {
		for true {
			ok := rpc.TestConn(host, port)
			if !ok {
				os.Exit(1)
			}
			time.Sleep(time.Duration(5) * time.Second)
		}
	}()

	go func() {
		handleCmd()
	}()

	fmt.Println("connect to ", host, ":", port)

	signalChan := make(chan os.Signal, 1) //创建一个信号量的chan，缓存为1，（0,1）意义不大

	signal.Notify(signalChan, syscall.SIGINT, syscall.SIGTERM) //让进城收集信号量。

	<-signalChan

	ExitFunc()

}

func ExitFunc() {
	fmt.Println("exit")
}

func handleCmd() {
	reader := bufio.NewReader(os.Stdin)
	for {
		fmt.Print("$ ")
		cmdString, err := reader.ReadString('\n')
		if err != nil {
			fmt.Fprintln(os.Stderr, err)
		}
		cmdString = strings.TrimSuffix(cmdString, "\n")
		if len(cmdString) != 0 {
			cmdex := parseCmd(cmdString)
			if cmdex != nil {
				cmdex.Run()
			}
		}
		if err != nil {
			fmt.Fprintln(os.Stderr, err)
		}
	}
}

func parseCmd(cmdString string) cmd.Run {
	cmdField := strings.Fields(cmdString)
	name := cmdField[0]
	switch name {
	case "showmodel":
		var showModelCmd cmd.ShowModelCmd
		showModelCmd.Name = name
		showModelCmd.Param = cmdField[1:]
		showModelCmd.Address = address
		return &showModelCmd
	case "showconfig":
		var showConfigCmd cmd.ShowConfigCmd
		showConfigCmd.Name = name
		showConfigCmd.Param = cmdField[1:]
		showConfigCmd.Address = address
		return &showConfigCmd
	case "inference":
		var inferenceCmd cmd.InferenceCmd
		inferenceCmd.Name = name
		inferenceCmd.Param = cmdField[1:]
		inferenceCmd.Address = address
		return &inferenceCmd
	case "batchInference":
		var batchInferenceCmd cmd.BatchInferenceCmd
		batchInferenceCmd.Name = name
		batchInferenceCmd.Param = cmdField[1:]
		batchInferenceCmd.Address = address
		return &batchInferenceCmd
	case "help":
		var helpCmd cmd.HelpCmd
		helpCmd.Name = name
		helpCmd.Param = cmdField[1:]
		helpCmd.Address = address
		return &helpCmd
	case "quit":
		var quitCmd cmd.QuitCmd
		quitCmd.Name = name
		quitCmd.Param = cmdField[1:]
		quitCmd.Address = address
		return &quitCmd

	default:
		fmt.Println("invalid cmd ", name, " ,now support showmodel/showconfig/help/quit")
	}
	return nil
}

func printUsage() {
	fmt.Println()

}
