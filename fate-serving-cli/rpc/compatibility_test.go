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

package rpc_test

import (
	"context"
	"net"
	"testing"

	"fate-serving-client/pb"
	"fate-serving-client/rpc"

	"google.golang.org/grpc"
	"google.golang.org/protobuf/proto"
)

type servingFixture struct {
	pb.UnimplementedCommonServiceServer
	pb.UnimplementedInferenceServiceServer
	pb.UnimplementedModelServiceServer
	pb.UnimplementedDataTransferServiceServer
}

func (*servingFixture) ListProps(_ context.Context, req *pb.QueryPropsRequest) (*pb.CommonResponse, error) {
	return &pb.CommonResponse{Data: []byte(req.GetKeyword())}, nil
}

func (*servingFixture) Inference(_ context.Context, req *pb.InferenceMessage) (*pb.InferenceMessage, error) {
	return &pb.InferenceMessage{Header: []byte("inference"), Body: req.GetBody()}, nil
}

func (*servingFixture) BatchInference(_ context.Context, req *pb.InferenceMessage) (*pb.InferenceMessage, error) {
	return &pb.InferenceMessage{Header: []byte("batch"), Body: req.GetBody()}, nil
}

func (*servingFixture) QueryModel(_ context.Context, req *pb.QueryModelRequest) (*pb.QueryModelResponse, error) {
	return &pb.QueryModelResponse{ModelInfos: []*pb.ModelInfoEx{{ServiceIds: []string{req.GetServiceId()}}}}, nil
}

func (*servingFixture) UnaryCall(_ context.Context, req *pb.Packet) (*pb.Packet, error) {
	return req, nil
}

func TestServingRPCCompatibility(t *testing.T) {
	listener, err := net.Listen("tcp4", "127.0.0.1:0")
	if err != nil {
		t.Fatal(err)
	}
	server := grpc.NewServer()
	fixture := &servingFixture{}
	pb.RegisterCommonServiceServer(server, fixture)
	pb.RegisterInferenceServiceServer(server, fixture)
	pb.RegisterModelServiceServer(server, fixture)
	pb.RegisterDataTransferServiceServer(server, fixture)
	done := make(chan error, 1)
	go func() { done <- server.Serve(listener) }()
	t.Cleanup(func() {
		server.Stop()
		if err := <-done; err != nil && err != grpc.ErrServerStopped {
			t.Errorf("gRPC server: %v", err)
		}
	})

	address := listener.Addr().String()
	t.Run("config", func(t *testing.T) {
		got, err := rpc.QueryServingServerConfig(address, &pb.QueryPropsRequest{Keyword: "fixture"})
		if err != nil || string(got.GetData()) != "fixture" {
			t.Fatalf("config = %v, error = %v", got, err)
		}
	})
	t.Run("model", func(t *testing.T) {
		got, err := rpc.QueryModelInfo(address, &pb.QueryModelRequest{ServiceId: "fixture"})
		want := &pb.QueryModelResponse{ModelInfos: []*pb.ModelInfoEx{{ServiceIds: []string{"fixture"}}}}
		if err != nil || !proto.Equal(got, want) {
			t.Fatalf("model = %v, error = %v", got, err)
		}
	})
	for name, call := range map[string]func(string, *pb.InferenceMessage) (*pb.InferenceMessage, error){
		"inference": rpc.Inference,
		"batch":     rpc.BatchInference,
	} {
		t.Run(name, func(t *testing.T) {
			got, err := call(address, &pb.InferenceMessage{Body: []byte("fixture")})
			want := &pb.InferenceMessage{Header: []byte(name), Body: []byte("fixture")}
			if err != nil || !proto.Equal(got, want) {
				t.Fatalf("inference = %v, error = %v", got, err)
			}
		})
	}
	t.Run("legacy proxy packet", func(t *testing.T) {
		packet := &pb.Packet{
			Header: &pb.Metadata{Src: &pb.Topic{PartyId: "10000"}},
			Body:   &pb.Data{Key: "fixture", Value: []byte("hello")},
		}
		got, err := rpc.UnaryCall(address, packet)
		if err != nil || got.GetHeader().GetSrc().GetPartyId() != "10000" ||
			got.GetBody().GetKey() != "fixture" || string(got.GetBody().GetValue()) != "hello" {
			t.Fatalf("legacy proxy packet = %v, error = %v", got, err)
		}
	})
}
