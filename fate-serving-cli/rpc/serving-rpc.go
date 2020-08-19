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

func BatchInference(address string, message *pb.InferenceMessage) (*pb.InferenceMessage, error) {
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
	r, err := c.BatchInference(ctx, message)
	if err != nil {
		return nil, err
	}
	return r, nil
}

func usage() {
	fmt.Fprintf(os.Stderr, `Usage:  [-h host] [-p port]`)
}

func TestConn(host string, port int) bool {
	portString := strconv.Itoa(port)
	_, err := net.Dial("tcp", host+":"+portString)
	if err != nil {
		fmt.Println("\n\rcan not connect to ", host, ":", portString)
		usage()
		//os.Exit(1)
		return false
	} else {
		return true
	}
}
