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
	"time"

	"google.golang.org/grpc"
)

func UnaryCall(address string, packet *pb.Packet) (*pb.Packet, error) {
	//glogger.Info("try to UnaryCall,address",address)
	var conn *grpc.ClientConn
	var err error

	defer panicRecover()

	conn, err = grpc.Dial(address, grpc.WithInsecure())

	if err != nil {
		return nil, err
	}
	defer func() {
		if conn != nil {
			conn.Close()
		}
	}()
	c := pb.NewDataTransferServiceClient(conn)
	ctx, cancel := context.WithTimeout(context.Background(), 3*time.Second)
	defer cancel()

	r, err := c.UnaryCall(ctx, packet)
	if err != nil {
		return nil, err
	}
	return r, nil
}
