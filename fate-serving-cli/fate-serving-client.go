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
	rpc.TestConn(host, port)
	go func() {
		handleCmd()

	}()

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
