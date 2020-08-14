package rpc

import (
	"context"
	"fate-serving-client/pb"
	"fmt"
	"google.golang.org/grpc"
	"net"
	"os"
	"strconv"
	"time"
)

func QueryServingServerConfig(address string, queryPropsRequest *pb.QueryPropsRequest) (*pb.CommonResponse, error) {
	//glogger.Info("try to UnaryCall,address",address)
	var conn *grpc.ClientConn
	var err error

	conn, err = grpc.Dial(address, grpc.WithInsecure())

	if err != nil {
		return nil, err
	}
	defer func() {
		if conn != nil {
			conn.Close()
		}
	}()
	c := pb.NewCommonServiceClient(conn)
	ctx, cancel := context.WithTimeout(context.Background(), 3*time.Second)
	defer cancel()
	r, err := c.ListProps(ctx, queryPropsRequest)
	if err != nil {
		return nil, err
	}
	return r, nil
}

func QueryModelInfo(address string, queryModelRequest *pb.QueryModelRequest) (*pb.QueryModelResponse, error) {
	var conn *grpc.ClientConn
	var err error

	conn, err = grpc.Dial(address, grpc.WithInsecure())

	if err != nil {
		return nil, err
	}
	defer func() {
		if conn != nil {
			conn.Close()
		}
	}()
	c := pb.NewModelServiceClient(conn)
	ctx, cancel := context.WithTimeout(context.Background(), 3*time.Second)
	defer cancel()
	r, err := c.QueryModel(ctx, queryModelRequest)
	if err != nil {
		return nil, err
	}
	return r, nil
}

func Inference(address string, message *pb.InferenceMessage) (*pb.InferenceMessage, error) {
	var conn *grpc.ClientConn
	var err error

	conn, err = grpc.Dial(address, grpc.WithInsecure())
	if err != nil {
		return nil, err
	}

	defer func() {
		if conn != nil {
			conn.Close()
		}
	}()
	c := pb.NewInferenceServiceClient(conn)
	ctx, cancel := context.WithTimeout(context.Background(), 3*time.Second)
	defer cancel()
	r, err := c.Inference(ctx, message)
	if err != nil {
		return nil, err
	}
	return r, nil
}

func usage() {
	fmt.Fprintf(os.Stderr, `
Usage:  [-h host] [-p port]
`)

}

func TestConn(host string, port int) {
	portString := strconv.Itoa(port)
	_, err := net.Dial("tcp", host+":"+portString)
	if err != nil {
		fmt.Println("can not connect to ", host, ":", portString)
		usage()
		os.Exit(1)
	} else {
		fmt.Println("connect to ", host, ":", portString)
	}
}
