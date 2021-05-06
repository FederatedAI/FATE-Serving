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

package pb

import (
	context "context"
	proto "github.com/golang/protobuf/proto"
	grpc "google.golang.org/grpc"
	codes "google.golang.org/grpc/codes"
	status "google.golang.org/grpc/status"
	protoreflect "google.golang.org/protobuf/reflect/protoreflect"
	protoimpl "google.golang.org/protobuf/runtime/protoimpl"
	reflect "reflect"
	sync "sync"
)

const (
	// Verify that this generated code is sufficiently up-to-date.
	_ = protoimpl.EnforceVersion(20 - protoimpl.MinVersion)
	// Verify that runtime/protoimpl is sufficiently up-to-date.
	_ = protoimpl.EnforceVersion(protoimpl.MaxVersion - 20)
)

// This is a compile-time assertion that a sufficiently up-to-date version
// of the legacy proto package is being used.
const _ = proto.ProtoPackageIsVersion4

type InferenceMessage struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	Header []byte `protobuf:"bytes,1,opt,name=header,proto3" json:"header,omitempty"`
	Body   []byte `protobuf:"bytes,2,opt,name=body,proto3" json:"body,omitempty"`
}

func (x *InferenceMessage) Reset() {
	*x = InferenceMessage{}
	if protoimpl.UnsafeEnabled {
		mi := &file_inference_service_proto_msgTypes[0]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *InferenceMessage) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*InferenceMessage) ProtoMessage() {}

func (x *InferenceMessage) ProtoReflect() protoreflect.Message {
	mi := &file_inference_service_proto_msgTypes[0]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use InferenceMessage.ProtoReflect.Descriptor instead.
func (*InferenceMessage) Descriptor() ([]byte, []int) {
	return file_inference_service_proto_rawDescGZIP(), []int{0}
}

func (x *InferenceMessage) GetHeader() []byte {
	if x != nil {
		return x.Header
	}
	return nil
}

func (x *InferenceMessage) GetBody() []byte {
	if x != nil {
		return x.Body
	}
	return nil
}

var File_inference_service_proto protoreflect.FileDescriptor

var file_inference_service_proto_rawDesc = []byte{
	0x0a, 0x17, 0x69, 0x6e, 0x66, 0x65, 0x72, 0x65, 0x6e, 0x63, 0x65, 0x5f, 0x73, 0x65, 0x72, 0x76,
	0x69, 0x63, 0x65, 0x2e, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x12, 0x1e, 0x63, 0x6f, 0x6d, 0x2e, 0x77,
	0x65, 0x62, 0x61, 0x6e, 0x6b, 0x2e, 0x61, 0x69, 0x2e, 0x66, 0x61, 0x74, 0x65, 0x2e, 0x61, 0x70,
	0x69, 0x2e, 0x73, 0x65, 0x72, 0x76, 0x69, 0x6e, 0x67, 0x22, 0x3e, 0x0a, 0x10, 0x49, 0x6e, 0x66,
	0x65, 0x72, 0x65, 0x6e, 0x63, 0x65, 0x4d, 0x65, 0x73, 0x73, 0x61, 0x67, 0x65, 0x12, 0x16, 0x0a,
	0x06, 0x68, 0x65, 0x61, 0x64, 0x65, 0x72, 0x18, 0x01, 0x20, 0x01, 0x28, 0x0c, 0x52, 0x06, 0x68,
	0x65, 0x61, 0x64, 0x65, 0x72, 0x12, 0x12, 0x0a, 0x04, 0x62, 0x6f, 0x64, 0x79, 0x18, 0x02, 0x20,
	0x01, 0x28, 0x0c, 0x52, 0x04, 0x62, 0x6f, 0x64, 0x79, 0x32, 0xf9, 0x01, 0x0a, 0x10, 0x49, 0x6e,
	0x66, 0x65, 0x72, 0x65, 0x6e, 0x63, 0x65, 0x53, 0x65, 0x72, 0x76, 0x69, 0x63, 0x65, 0x12, 0x74,
	0x0a, 0x0e, 0x62, 0x61, 0x74, 0x63, 0x68, 0x49, 0x6e, 0x66, 0x65, 0x72, 0x65, 0x6e, 0x63, 0x65,
	0x12, 0x30, 0x2e, 0x63, 0x6f, 0x6d, 0x2e, 0x77, 0x65, 0x62, 0x61, 0x6e, 0x6b, 0x2e, 0x61, 0x69,
	0x2e, 0x66, 0x61, 0x74, 0x65, 0x2e, 0x61, 0x70, 0x69, 0x2e, 0x73, 0x65, 0x72, 0x76, 0x69, 0x6e,
	0x67, 0x2e, 0x49, 0x6e, 0x66, 0x65, 0x72, 0x65, 0x6e, 0x63, 0x65, 0x4d, 0x65, 0x73, 0x73, 0x61,
	0x67, 0x65, 0x1a, 0x30, 0x2e, 0x63, 0x6f, 0x6d, 0x2e, 0x77, 0x65, 0x62, 0x61, 0x6e, 0x6b, 0x2e,
	0x61, 0x69, 0x2e, 0x66, 0x61, 0x74, 0x65, 0x2e, 0x61, 0x70, 0x69, 0x2e, 0x73, 0x65, 0x72, 0x76,
	0x69, 0x6e, 0x67, 0x2e, 0x49, 0x6e, 0x66, 0x65, 0x72, 0x65, 0x6e, 0x63, 0x65, 0x4d, 0x65, 0x73,
	0x73, 0x61, 0x67, 0x65, 0x12, 0x6f, 0x0a, 0x09, 0x69, 0x6e, 0x66, 0x65, 0x72, 0x65, 0x6e, 0x63,
	0x65, 0x12, 0x30, 0x2e, 0x63, 0x6f, 0x6d, 0x2e, 0x77, 0x65, 0x62, 0x61, 0x6e, 0x6b, 0x2e, 0x61,
	0x69, 0x2e, 0x66, 0x61, 0x74, 0x65, 0x2e, 0x61, 0x70, 0x69, 0x2e, 0x73, 0x65, 0x72, 0x76, 0x69,
	0x6e, 0x67, 0x2e, 0x49, 0x6e, 0x66, 0x65, 0x72, 0x65, 0x6e, 0x63, 0x65, 0x4d, 0x65, 0x73, 0x73,
	0x61, 0x67, 0x65, 0x1a, 0x30, 0x2e, 0x63, 0x6f, 0x6d, 0x2e, 0x77, 0x65, 0x62, 0x61, 0x6e, 0x6b,
	0x2e, 0x61, 0x69, 0x2e, 0x66, 0x61, 0x74, 0x65, 0x2e, 0x61, 0x70, 0x69, 0x2e, 0x73, 0x65, 0x72,
	0x76, 0x69, 0x6e, 0x67, 0x2e, 0x49, 0x6e, 0x66, 0x65, 0x72, 0x65, 0x6e, 0x63, 0x65, 0x4d, 0x65,
	0x73, 0x73, 0x61, 0x67, 0x65, 0x42, 0x17, 0x42, 0x15, 0x49, 0x6e, 0x66, 0x65, 0x72, 0x65, 0x6e,
	0x63, 0x65, 0x53, 0x65, 0x72, 0x76, 0x69, 0x63, 0x65, 0x50, 0x72, 0x6f, 0x74, 0x6f, 0x62, 0x06,
	0x70, 0x72, 0x6f, 0x74, 0x6f, 0x33,
}

var (
	file_inference_service_proto_rawDescOnce sync.Once
	file_inference_service_proto_rawDescData = file_inference_service_proto_rawDesc
)

func file_inference_service_proto_rawDescGZIP() []byte {
	file_inference_service_proto_rawDescOnce.Do(func() {
		file_inference_service_proto_rawDescData = protoimpl.X.CompressGZIP(file_inference_service_proto_rawDescData)
	})
	return file_inference_service_proto_rawDescData
}

var file_inference_service_proto_msgTypes = make([]protoimpl.MessageInfo, 1)
var file_inference_service_proto_goTypes = []interface{}{
	(*InferenceMessage)(nil), // 0: com.webank.ai.fate.api.serving.InferenceMessage
}
var file_inference_service_proto_depIdxs = []int32{
	0, // 0: com.webank.ai.fate.api.serving.InferenceService.batchInference:input_type -> com.webank.ai.fate.api.serving.InferenceMessage
	0, // 1: com.webank.ai.fate.api.serving.InferenceService.inference:input_type -> com.webank.ai.fate.api.serving.InferenceMessage
	0, // 2: com.webank.ai.fate.api.serving.InferenceService.batchInference:output_type -> com.webank.ai.fate.api.serving.InferenceMessage
	0, // 3: com.webank.ai.fate.api.serving.InferenceService.inference:output_type -> com.webank.ai.fate.api.serving.InferenceMessage
	2, // [2:4] is the sub-list for method output_type
	0, // [0:2] is the sub-list for method input_type
	0, // [0:0] is the sub-list for extension type_name
	0, // [0:0] is the sub-list for extension extendee
	0, // [0:0] is the sub-list for field type_name
}

func init() { file_inference_service_proto_init() }
func file_inference_service_proto_init() {
	if File_inference_service_proto != nil {
		return
	}
	if !protoimpl.UnsafeEnabled {
		file_inference_service_proto_msgTypes[0].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*InferenceMessage); i {
			case 0:
				return &v.state
			case 1:
				return &v.sizeCache
			case 2:
				return &v.unknownFields
			default:
				return nil
			}
		}
	}
	type x struct{}
	out := protoimpl.TypeBuilder{
		File: protoimpl.DescBuilder{
			GoPackagePath: reflect.TypeOf(x{}).PkgPath(),
			RawDescriptor: file_inference_service_proto_rawDesc,
			NumEnums:      0,
			NumMessages:   1,
			NumExtensions: 0,
			NumServices:   1,
		},
		GoTypes:           file_inference_service_proto_goTypes,
		DependencyIndexes: file_inference_service_proto_depIdxs,
		MessageInfos:      file_inference_service_proto_msgTypes,
	}.Build()
	File_inference_service_proto = out.File
	file_inference_service_proto_rawDesc = nil
	file_inference_service_proto_goTypes = nil
	file_inference_service_proto_depIdxs = nil
}

// InferenceServiceClient is the client API for InferenceService service.
//
// For semantics around ctx use and closing/ending streaming RPCs, please refer to https://pkg.go.dev/google.golang.org/grpc/?tab=doc#ClientConn.NewStream.
type InferenceServiceClient interface {
	BatchInference(ctx context.Context, in *InferenceMessage, opts ...grpc.CallOption) (*InferenceMessage, error)
	Inference(ctx context.Context, in *InferenceMessage, opts ...grpc.CallOption) (*InferenceMessage, error)
}

type inferenceServiceClient struct {
	cc grpc.ClientConnInterface
}

func NewInferenceServiceClient(cc grpc.ClientConnInterface) InferenceServiceClient {
	return &inferenceServiceClient{cc}
}

func (c *inferenceServiceClient) BatchInference(ctx context.Context, in *InferenceMessage, opts ...grpc.CallOption) (*InferenceMessage, error) {
	out := new(InferenceMessage)
	err := c.cc.Invoke(ctx, "/com.webank.ai.fate.api.serving.InferenceService/batchInference", in, out, opts...)
	if err != nil {
		return nil, err
	}
	return out, nil
}

func (c *inferenceServiceClient) Inference(ctx context.Context, in *InferenceMessage, opts ...grpc.CallOption) (*InferenceMessage, error) {
	out := new(InferenceMessage)
	err := c.cc.Invoke(ctx, "/com.webank.ai.fate.api.serving.InferenceService/inference", in, out, opts...)
	if err != nil {
		return nil, err
	}
	return out, nil
}

// InferenceServiceServer is the server API for InferenceService service.
// All implementations must embed UnimplementedInferenceServiceServer
// for forward compatibility
type InferenceServiceServer interface {
	BatchInference(context.Context, *InferenceMessage) (*InferenceMessage, error)
	Inference(context.Context, *InferenceMessage) (*InferenceMessage, error)
	mustEmbedUnimplementedInferenceServiceServer()
}

// UnimplementedInferenceServiceServer must be embedded to have forward compatible implementations.
type UnimplementedInferenceServiceServer struct {
}

func (*UnimplementedInferenceServiceServer) BatchInference(context.Context, *InferenceMessage) (*InferenceMessage, error) {
	return nil, status.Errorf(codes.Unimplemented, "method BatchInference not implemented")
}
func (*UnimplementedInferenceServiceServer) Inference(context.Context, *InferenceMessage) (*InferenceMessage, error) {
	return nil, status.Errorf(codes.Unimplemented, "method Inference not implemented")
}
func (*UnimplementedInferenceServiceServer) mustEmbedUnimplementedInferenceServiceServer() {}

func RegisterInferenceServiceServer(s *grpc.Server, srv InferenceServiceServer) {
	s.RegisterService(&_InferenceService_serviceDesc, srv)
}

func _InferenceService_BatchInference_Handler(srv interface{}, ctx context.Context, dec func(interface{}) error, interceptor grpc.UnaryServerInterceptor) (interface{}, error) {
	in := new(InferenceMessage)
	if err := dec(in); err != nil {
		return nil, err
	}
	if interceptor == nil {
		return srv.(InferenceServiceServer).BatchInference(ctx, in)
	}
	info := &grpc.UnaryServerInfo{
		Server:     srv,
		FullMethod: "/com.webank.ai.fate.api.serving.InferenceService/BatchInference",
	}
	handler := func(ctx context.Context, req interface{}) (interface{}, error) {
		return srv.(InferenceServiceServer).BatchInference(ctx, req.(*InferenceMessage))
	}
	return interceptor(ctx, in, info, handler)
}

func _InferenceService_Inference_Handler(srv interface{}, ctx context.Context, dec func(interface{}) error, interceptor grpc.UnaryServerInterceptor) (interface{}, error) {
	in := new(InferenceMessage)
	if err := dec(in); err != nil {
		return nil, err
	}
	if interceptor == nil {
		return srv.(InferenceServiceServer).Inference(ctx, in)
	}
	info := &grpc.UnaryServerInfo{
		Server:     srv,
		FullMethod: "/com.webank.ai.fate.api.serving.InferenceService/Inference",
	}
	handler := func(ctx context.Context, req interface{}) (interface{}, error) {
		return srv.(InferenceServiceServer).Inference(ctx, req.(*InferenceMessage))
	}
	return interceptor(ctx, in, info, handler)
}

var _InferenceService_serviceDesc = grpc.ServiceDesc{
	ServiceName: "com.webank.ai.fate.api.serving.InferenceService",
	HandlerType: (*InferenceServiceServer)(nil),
	Methods: []grpc.MethodDesc{
		{
			MethodName: "batchInference",
			Handler:    _InferenceService_BatchInference_Handler,
		},
		{
			MethodName: "inference",
			Handler:    _InferenceService_Inference_Handler,
		},
	},
	Streams:  []grpc.StreamDesc{},
	Metadata: "inference_service.proto",
}
