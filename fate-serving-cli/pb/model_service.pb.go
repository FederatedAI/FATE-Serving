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
	fmt "fmt"
	proto "github.com/golang/protobuf/proto"
	grpc "google.golang.org/grpc"
	codes "google.golang.org/grpc/codes"
	status "google.golang.org/grpc/status"
	math "math"
)

// Reference imports to suppress errors if they are not otherwise used.
var _ = proto.Marshal
var _ = fmt.Errorf
var _ = math.Inf

// This is a compile-time assertion to ensure that this generated file
// is compatible with the proto package it is being compiled against.
// A compilation error at this line likely means your copy of the
// proto package needs to be updated.
const _ = proto.ProtoPackageIsVersion3 // please upgrade the proto package

type Party struct {
	PartyId              []string `protobuf:"bytes,1,rep,name=partyId,proto3" json:"partyId,omitempty"`
	XXX_NoUnkeyedLiteral struct{} `json:"-"`
	XXX_unrecognized     []byte   `json:"-"`
	XXX_sizecache        int32    `json:"-"`
}

func (m *Party) Reset()         { *m = Party{} }
func (m *Party) String() string { return proto.CompactTextString(m) }
func (*Party) ProtoMessage()    {}
func (*Party) Descriptor() ([]byte, []int) {
	return fileDescriptor_b8ac95aa9096eaec, []int{0}
}

func (m *Party) XXX_Unmarshal(b []byte) error {
	return xxx_messageInfo_Party.Unmarshal(m, b)
}
func (m *Party) XXX_Marshal(b []byte, deterministic bool) ([]byte, error) {
	return xxx_messageInfo_Party.Marshal(b, m, deterministic)
}
func (m *Party) XXX_Merge(src proto.Message) {
	xxx_messageInfo_Party.Merge(m, src)
}
func (m *Party) XXX_Size() int {
	return xxx_messageInfo_Party.Size(m)
}
func (m *Party) XXX_DiscardUnknown() {
	xxx_messageInfo_Party.DiscardUnknown(m)
}

var xxx_messageInfo_Party proto.InternalMessageInfo

func (m *Party) GetPartyId() []string {
	if m != nil {
		return m.PartyId
	}
	return nil
}

type LocalInfo struct {
	Role                 string   `protobuf:"bytes,1,opt,name=role,proto3" json:"role,omitempty"`
	PartyId              string   `protobuf:"bytes,2,opt,name=partyId,proto3" json:"partyId,omitempty"`
	XXX_NoUnkeyedLiteral struct{} `json:"-"`
	XXX_unrecognized     []byte   `json:"-"`
	XXX_sizecache        int32    `json:"-"`
}

func (m *LocalInfo) Reset()         { *m = LocalInfo{} }
func (m *LocalInfo) String() string { return proto.CompactTextString(m) }
func (*LocalInfo) ProtoMessage()    {}
func (*LocalInfo) Descriptor() ([]byte, []int) {
	return fileDescriptor_b8ac95aa9096eaec, []int{1}
}

func (m *LocalInfo) XXX_Unmarshal(b []byte) error {
	return xxx_messageInfo_LocalInfo.Unmarshal(m, b)
}
func (m *LocalInfo) XXX_Marshal(b []byte, deterministic bool) ([]byte, error) {
	return xxx_messageInfo_LocalInfo.Marshal(b, m, deterministic)
}
func (m *LocalInfo) XXX_Merge(src proto.Message) {
	xxx_messageInfo_LocalInfo.Merge(m, src)
}
func (m *LocalInfo) XXX_Size() int {
	return xxx_messageInfo_LocalInfo.Size(m)
}
func (m *LocalInfo) XXX_DiscardUnknown() {
	xxx_messageInfo_LocalInfo.DiscardUnknown(m)
}

var xxx_messageInfo_LocalInfo proto.InternalMessageInfo

func (m *LocalInfo) GetRole() string {
	if m != nil {
		return m.Role
	}
	return ""
}

func (m *LocalInfo) GetPartyId() string {
	if m != nil {
		return m.PartyId
	}
	return ""
}

type ModelInfo struct {
	TableName            string   `protobuf:"bytes,1,opt,name=tableName,proto3" json:"tableName,omitempty"`
	Namespace            string   `protobuf:"bytes,2,opt,name=namespace,proto3" json:"namespace,omitempty"`
	XXX_NoUnkeyedLiteral struct{} `json:"-"`
	XXX_unrecognized     []byte   `json:"-"`
	XXX_sizecache        int32    `json:"-"`
}

func (m *ModelInfo) Reset()         { *m = ModelInfo{} }
func (m *ModelInfo) String() string { return proto.CompactTextString(m) }
func (*ModelInfo) ProtoMessage()    {}
func (*ModelInfo) Descriptor() ([]byte, []int) {
	return fileDescriptor_b8ac95aa9096eaec, []int{2}
}

func (m *ModelInfo) XXX_Unmarshal(b []byte) error {
	return xxx_messageInfo_ModelInfo.Unmarshal(m, b)
}
func (m *ModelInfo) XXX_Marshal(b []byte, deterministic bool) ([]byte, error) {
	return xxx_messageInfo_ModelInfo.Marshal(b, m, deterministic)
}
func (m *ModelInfo) XXX_Merge(src proto.Message) {
	xxx_messageInfo_ModelInfo.Merge(m, src)
}
func (m *ModelInfo) XXX_Size() int {
	return xxx_messageInfo_ModelInfo.Size(m)
}
func (m *ModelInfo) XXX_DiscardUnknown() {
	xxx_messageInfo_ModelInfo.DiscardUnknown(m)
}

var xxx_messageInfo_ModelInfo proto.InternalMessageInfo

func (m *ModelInfo) GetTableName() string {
	if m != nil {
		return m.TableName
	}
	return ""
}

func (m *ModelInfo) GetNamespace() string {
	if m != nil {
		return m.Namespace
	}
	return ""
}

type RoleModelInfo struct {
	RoleModelInfo        map[string]*ModelInfo `protobuf:"bytes,1,rep,name=roleModelInfo,proto3" json:"roleModelInfo,omitempty" protobuf_key:"bytes,1,opt,name=key,proto3" protobuf_val:"bytes,2,opt,name=value,proto3"`
	XXX_NoUnkeyedLiteral struct{}              `json:"-"`
	XXX_unrecognized     []byte                `json:"-"`
	XXX_sizecache        int32                 `json:"-"`
}

func (m *RoleModelInfo) Reset()         { *m = RoleModelInfo{} }
func (m *RoleModelInfo) String() string { return proto.CompactTextString(m) }
func (*RoleModelInfo) ProtoMessage()    {}
func (*RoleModelInfo) Descriptor() ([]byte, []int) {
	return fileDescriptor_b8ac95aa9096eaec, []int{3}
}

func (m *RoleModelInfo) XXX_Unmarshal(b []byte) error {
	return xxx_messageInfo_RoleModelInfo.Unmarshal(m, b)
}
func (m *RoleModelInfo) XXX_Marshal(b []byte, deterministic bool) ([]byte, error) {
	return xxx_messageInfo_RoleModelInfo.Marshal(b, m, deterministic)
}
func (m *RoleModelInfo) XXX_Merge(src proto.Message) {
	xxx_messageInfo_RoleModelInfo.Merge(m, src)
}
func (m *RoleModelInfo) XXX_Size() int {
	return xxx_messageInfo_RoleModelInfo.Size(m)
}
func (m *RoleModelInfo) XXX_DiscardUnknown() {
	xxx_messageInfo_RoleModelInfo.DiscardUnknown(m)
}

var xxx_messageInfo_RoleModelInfo proto.InternalMessageInfo

func (m *RoleModelInfo) GetRoleModelInfo() map[string]*ModelInfo {
	if m != nil {
		return m.RoleModelInfo
	}
	return nil
}

type UnloadRequest struct {
	TableName            string   `protobuf:"bytes,1,opt,name=tableName,proto3" json:"tableName,omitempty"`
	Namespace            string   `protobuf:"bytes,2,opt,name=namespace,proto3" json:"namespace,omitempty"`
	XXX_NoUnkeyedLiteral struct{} `json:"-"`
	XXX_unrecognized     []byte   `json:"-"`
	XXX_sizecache        int32    `json:"-"`
}

func (m *UnloadRequest) Reset()         { *m = UnloadRequest{} }
func (m *UnloadRequest) String() string { return proto.CompactTextString(m) }
func (*UnloadRequest) ProtoMessage()    {}
func (*UnloadRequest) Descriptor() ([]byte, []int) {
	return fileDescriptor_b8ac95aa9096eaec, []int{4}
}

func (m *UnloadRequest) XXX_Unmarshal(b []byte) error {
	return xxx_messageInfo_UnloadRequest.Unmarshal(m, b)
}
func (m *UnloadRequest) XXX_Marshal(b []byte, deterministic bool) ([]byte, error) {
	return xxx_messageInfo_UnloadRequest.Marshal(b, m, deterministic)
}
func (m *UnloadRequest) XXX_Merge(src proto.Message) {
	xxx_messageInfo_UnloadRequest.Merge(m, src)
}
func (m *UnloadRequest) XXX_Size() int {
	return xxx_messageInfo_UnloadRequest.Size(m)
}
func (m *UnloadRequest) XXX_DiscardUnknown() {
	xxx_messageInfo_UnloadRequest.DiscardUnknown(m)
}

var xxx_messageInfo_UnloadRequest proto.InternalMessageInfo

func (m *UnloadRequest) GetTableName() string {
	if m != nil {
		return m.TableName
	}
	return ""
}

func (m *UnloadRequest) GetNamespace() string {
	if m != nil {
		return m.Namespace
	}
	return ""
}

type UnloadResponse struct {
	StatusCode           int32    `protobuf:"varint,1,opt,name=statusCode,proto3" json:"statusCode,omitempty"`
	Message              string   `protobuf:"bytes,2,opt,name=message,proto3" json:"message,omitempty"`
	XXX_NoUnkeyedLiteral struct{} `json:"-"`
	XXX_unrecognized     []byte   `json:"-"`
	XXX_sizecache        int32    `json:"-"`
}

func (m *UnloadResponse) Reset()         { *m = UnloadResponse{} }
func (m *UnloadResponse) String() string { return proto.CompactTextString(m) }
func (*UnloadResponse) ProtoMessage()    {}
func (*UnloadResponse) Descriptor() ([]byte, []int) {
	return fileDescriptor_b8ac95aa9096eaec, []int{5}
}

func (m *UnloadResponse) XXX_Unmarshal(b []byte) error {
	return xxx_messageInfo_UnloadResponse.Unmarshal(m, b)
}
func (m *UnloadResponse) XXX_Marshal(b []byte, deterministic bool) ([]byte, error) {
	return xxx_messageInfo_UnloadResponse.Marshal(b, m, deterministic)
}
func (m *UnloadResponse) XXX_Merge(src proto.Message) {
	xxx_messageInfo_UnloadResponse.Merge(m, src)
}
func (m *UnloadResponse) XXX_Size() int {
	return xxx_messageInfo_UnloadResponse.Size(m)
}
func (m *UnloadResponse) XXX_DiscardUnknown() {
	xxx_messageInfo_UnloadResponse.DiscardUnknown(m)
}

var xxx_messageInfo_UnloadResponse proto.InternalMessageInfo

func (m *UnloadResponse) GetStatusCode() int32 {
	if m != nil {
		return m.StatusCode
	}
	return 0
}

func (m *UnloadResponse) GetMessage() string {
	if m != nil {
		return m.Message
	}
	return ""
}

type UnbindRequest struct {
	ServiceIds           []string `protobuf:"bytes,1,rep,name=serviceIds,proto3" json:"serviceIds,omitempty"`
	TableName            string   `protobuf:"bytes,2,opt,name=tableName,proto3" json:"tableName,omitempty"`
	Namespace            string   `protobuf:"bytes,3,opt,name=namespace,proto3" json:"namespace,omitempty"`
	XXX_NoUnkeyedLiteral struct{} `json:"-"`
	XXX_unrecognized     []byte   `json:"-"`
	XXX_sizecache        int32    `json:"-"`
}

func (m *UnbindRequest) Reset()         { *m = UnbindRequest{} }
func (m *UnbindRequest) String() string { return proto.CompactTextString(m) }
func (*UnbindRequest) ProtoMessage()    {}
func (*UnbindRequest) Descriptor() ([]byte, []int) {
	return fileDescriptor_b8ac95aa9096eaec, []int{6}
}

func (m *UnbindRequest) XXX_Unmarshal(b []byte) error {
	return xxx_messageInfo_UnbindRequest.Unmarshal(m, b)
}
func (m *UnbindRequest) XXX_Marshal(b []byte, deterministic bool) ([]byte, error) {
	return xxx_messageInfo_UnbindRequest.Marshal(b, m, deterministic)
}
func (m *UnbindRequest) XXX_Merge(src proto.Message) {
	xxx_messageInfo_UnbindRequest.Merge(m, src)
}
func (m *UnbindRequest) XXX_Size() int {
	return xxx_messageInfo_UnbindRequest.Size(m)
}
func (m *UnbindRequest) XXX_DiscardUnknown() {
	xxx_messageInfo_UnbindRequest.DiscardUnknown(m)
}

var xxx_messageInfo_UnbindRequest proto.InternalMessageInfo

func (m *UnbindRequest) GetServiceIds() []string {
	if m != nil {
		return m.ServiceIds
	}
	return nil
}

func (m *UnbindRequest) GetTableName() string {
	if m != nil {
		return m.TableName
	}
	return ""
}

func (m *UnbindRequest) GetNamespace() string {
	if m != nil {
		return m.Namespace
	}
	return ""
}

type UnbindResponse struct {
	StatusCode           int32    `protobuf:"varint,1,opt,name=statusCode,proto3" json:"statusCode,omitempty"`
	Message              string   `protobuf:"bytes,2,opt,name=message,proto3" json:"message,omitempty"`
	XXX_NoUnkeyedLiteral struct{} `json:"-"`
	XXX_unrecognized     []byte   `json:"-"`
	XXX_sizecache        int32    `json:"-"`
}

func (m *UnbindResponse) Reset()         { *m = UnbindResponse{} }
func (m *UnbindResponse) String() string { return proto.CompactTextString(m) }
func (*UnbindResponse) ProtoMessage()    {}
func (*UnbindResponse) Descriptor() ([]byte, []int) {
	return fileDescriptor_b8ac95aa9096eaec, []int{7}
}

func (m *UnbindResponse) XXX_Unmarshal(b []byte) error {
	return xxx_messageInfo_UnbindResponse.Unmarshal(m, b)
}
func (m *UnbindResponse) XXX_Marshal(b []byte, deterministic bool) ([]byte, error) {
	return xxx_messageInfo_UnbindResponse.Marshal(b, m, deterministic)
}
func (m *UnbindResponse) XXX_Merge(src proto.Message) {
	xxx_messageInfo_UnbindResponse.Merge(m, src)
}
func (m *UnbindResponse) XXX_Size() int {
	return xxx_messageInfo_UnbindResponse.Size(m)
}
func (m *UnbindResponse) XXX_DiscardUnknown() {
	xxx_messageInfo_UnbindResponse.DiscardUnknown(m)
}

var xxx_messageInfo_UnbindResponse proto.InternalMessageInfo

func (m *UnbindResponse) GetStatusCode() int32 {
	if m != nil {
		return m.StatusCode
	}
	return 0
}

func (m *UnbindResponse) GetMessage() string {
	if m != nil {
		return m.Message
	}
	return ""
}

type QueryModelRequest struct {
	ServiceId            string   `protobuf:"bytes,1,opt,name=serviceId,proto3" json:"serviceId,omitempty"`
	TableName            string   `protobuf:"bytes,2,opt,name=tableName,proto3" json:"tableName,omitempty"`
	Namespace            string   `protobuf:"bytes,3,opt,name=namespace,proto3" json:"namespace,omitempty"`
	BeginIndex           int32    `protobuf:"varint,4,opt,name=beginIndex,proto3" json:"beginIndex,omitempty"`
	EndIndex             int32    `protobuf:"varint,5,opt,name=endIndex,proto3" json:"endIndex,omitempty"`
	QueryType            int32    `protobuf:"varint,6,opt,name=queryType,proto3" json:"queryType,omitempty"`
	XXX_NoUnkeyedLiteral struct{} `json:"-"`
	XXX_unrecognized     []byte   `json:"-"`
	XXX_sizecache        int32    `json:"-"`
}

func (m *QueryModelRequest) Reset()         { *m = QueryModelRequest{} }
func (m *QueryModelRequest) String() string { return proto.CompactTextString(m) }
func (*QueryModelRequest) ProtoMessage()    {}
func (*QueryModelRequest) Descriptor() ([]byte, []int) {
	return fileDescriptor_b8ac95aa9096eaec, []int{8}
}

func (m *QueryModelRequest) XXX_Unmarshal(b []byte) error {
	return xxx_messageInfo_QueryModelRequest.Unmarshal(m, b)
}
func (m *QueryModelRequest) XXX_Marshal(b []byte, deterministic bool) ([]byte, error) {
	return xxx_messageInfo_QueryModelRequest.Marshal(b, m, deterministic)
}
func (m *QueryModelRequest) XXX_Merge(src proto.Message) {
	xxx_messageInfo_QueryModelRequest.Merge(m, src)
}
func (m *QueryModelRequest) XXX_Size() int {
	return xxx_messageInfo_QueryModelRequest.Size(m)
}
func (m *QueryModelRequest) XXX_DiscardUnknown() {
	xxx_messageInfo_QueryModelRequest.DiscardUnknown(m)
}

var xxx_messageInfo_QueryModelRequest proto.InternalMessageInfo

func (m *QueryModelRequest) GetServiceId() string {
	if m != nil {
		return m.ServiceId
	}
	return ""
}

func (m *QueryModelRequest) GetTableName() string {
	if m != nil {
		return m.TableName
	}
	return ""
}

func (m *QueryModelRequest) GetNamespace() string {
	if m != nil {
		return m.Namespace
	}
	return ""
}

func (m *QueryModelRequest) GetBeginIndex() int32 {
	if m != nil {
		return m.BeginIndex
	}
	return 0
}

func (m *QueryModelRequest) GetEndIndex() int32 {
	if m != nil {
		return m.EndIndex
	}
	return 0
}

func (m *QueryModelRequest) GetQueryType() int32 {
	if m != nil {
		return m.QueryType
	}
	return 0
}

type ModelBindInfo struct {
	XXX_NoUnkeyedLiteral struct{} `json:"-"`
	XXX_unrecognized     []byte   `json:"-"`
	XXX_sizecache        int32    `json:"-"`
}

func (m *ModelBindInfo) Reset()         { *m = ModelBindInfo{} }
func (m *ModelBindInfo) String() string { return proto.CompactTextString(m) }
func (*ModelBindInfo) ProtoMessage()    {}
func (*ModelBindInfo) Descriptor() ([]byte, []int) {
	return fileDescriptor_b8ac95aa9096eaec, []int{9}
}

func (m *ModelBindInfo) XXX_Unmarshal(b []byte) error {
	return xxx_messageInfo_ModelBindInfo.Unmarshal(m, b)
}
func (m *ModelBindInfo) XXX_Marshal(b []byte, deterministic bool) ([]byte, error) {
	return xxx_messageInfo_ModelBindInfo.Marshal(b, m, deterministic)
}
func (m *ModelBindInfo) XXX_Merge(src proto.Message) {
	xxx_messageInfo_ModelBindInfo.Merge(m, src)
}
func (m *ModelBindInfo) XXX_Size() int {
	return xxx_messageInfo_ModelBindInfo.Size(m)
}
func (m *ModelBindInfo) XXX_DiscardUnknown() {
	xxx_messageInfo_ModelBindInfo.DiscardUnknown(m)
}

var xxx_messageInfo_ModelBindInfo proto.InternalMessageInfo

type ModelInfoEx struct {
	TableName            string   `protobuf:"bytes,1,opt,name=tableName,proto3" json:"tableName,omitempty"`
	Namespace            string   `protobuf:"bytes,2,opt,name=namespace,proto3" json:"namespace,omitempty"`
	ServiceIds           []string `protobuf:"bytes,3,rep,name=serviceIds,proto3" json:"serviceIds,omitempty"`
	Content              string   `protobuf:"bytes,4,opt,name=content,proto3" json:"content,omitempty"`
	Index                int32    `protobuf:"varint,5,opt,name=index,proto3" json:"index,omitempty"`
	XXX_NoUnkeyedLiteral struct{} `json:"-"`
	XXX_unrecognized     []byte   `json:"-"`
	XXX_sizecache        int32    `json:"-"`
}

func (m *ModelInfoEx) Reset()         { *m = ModelInfoEx{} }
func (m *ModelInfoEx) String() string { return proto.CompactTextString(m) }
func (*ModelInfoEx) ProtoMessage()    {}
func (*ModelInfoEx) Descriptor() ([]byte, []int) {
	return fileDescriptor_b8ac95aa9096eaec, []int{10}
}

func (m *ModelInfoEx) XXX_Unmarshal(b []byte) error {
	return xxx_messageInfo_ModelInfoEx.Unmarshal(m, b)
}
func (m *ModelInfoEx) XXX_Marshal(b []byte, deterministic bool) ([]byte, error) {
	return xxx_messageInfo_ModelInfoEx.Marshal(b, m, deterministic)
}
func (m *ModelInfoEx) XXX_Merge(src proto.Message) {
	xxx_messageInfo_ModelInfoEx.Merge(m, src)
}
func (m *ModelInfoEx) XXX_Size() int {
	return xxx_messageInfo_ModelInfoEx.Size(m)
}
func (m *ModelInfoEx) XXX_DiscardUnknown() {
	xxx_messageInfo_ModelInfoEx.DiscardUnknown(m)
}

var xxx_messageInfo_ModelInfoEx proto.InternalMessageInfo

func (m *ModelInfoEx) GetTableName() string {
	if m != nil {
		return m.TableName
	}
	return ""
}

func (m *ModelInfoEx) GetNamespace() string {
	if m != nil {
		return m.Namespace
	}
	return ""
}

func (m *ModelInfoEx) GetServiceIds() []string {
	if m != nil {
		return m.ServiceIds
	}
	return nil
}

func (m *ModelInfoEx) GetContent() string {
	if m != nil {
		return m.Content
	}
	return ""
}

func (m *ModelInfoEx) GetIndex() int32 {
	if m != nil {
		return m.Index
	}
	return 0
}

type QueryModelResponse struct {
	Retcode              int32          `protobuf:"varint,1,opt,name=retcode,proto3" json:"retcode,omitempty"`
	Message              string         `protobuf:"bytes,2,opt,name=message,proto3" json:"message,omitempty"`
	ModelInfos           []*ModelInfoEx `protobuf:"bytes,3,rep,name=modelInfos,proto3" json:"modelInfos,omitempty"`
	XXX_NoUnkeyedLiteral struct{}       `json:"-"`
	XXX_unrecognized     []byte         `json:"-"`
	XXX_sizecache        int32          `json:"-"`
}

func (m *QueryModelResponse) Reset()         { *m = QueryModelResponse{} }
func (m *QueryModelResponse) String() string { return proto.CompactTextString(m) }
func (*QueryModelResponse) ProtoMessage()    {}
func (*QueryModelResponse) Descriptor() ([]byte, []int) {
	return fileDescriptor_b8ac95aa9096eaec, []int{11}
}

func (m *QueryModelResponse) XXX_Unmarshal(b []byte) error {
	return xxx_messageInfo_QueryModelResponse.Unmarshal(m, b)
}
func (m *QueryModelResponse) XXX_Marshal(b []byte, deterministic bool) ([]byte, error) {
	return xxx_messageInfo_QueryModelResponse.Marshal(b, m, deterministic)
}
func (m *QueryModelResponse) XXX_Merge(src proto.Message) {
	xxx_messageInfo_QueryModelResponse.Merge(m, src)
}
func (m *QueryModelResponse) XXX_Size() int {
	return xxx_messageInfo_QueryModelResponse.Size(m)
}
func (m *QueryModelResponse) XXX_DiscardUnknown() {
	xxx_messageInfo_QueryModelResponse.DiscardUnknown(m)
}

var xxx_messageInfo_QueryModelResponse proto.InternalMessageInfo

func (m *QueryModelResponse) GetRetcode() int32 {
	if m != nil {
		return m.Retcode
	}
	return 0
}

func (m *QueryModelResponse) GetMessage() string {
	if m != nil {
		return m.Message
	}
	return ""
}

func (m *QueryModelResponse) GetModelInfos() []*ModelInfoEx {
	if m != nil {
		return m.ModelInfos
	}
	return nil
}

type PublishRequest struct {
	Local                *LocalInfo                `protobuf:"bytes,1,opt,name=local,proto3" json:"local,omitempty"`
	Role                 map[string]*Party         `protobuf:"bytes,2,rep,name=role,proto3" json:"role,omitempty" protobuf_key:"bytes,1,opt,name=key,proto3" protobuf_val:"bytes,2,opt,name=value,proto3"`
	Model                map[string]*RoleModelInfo `protobuf:"bytes,3,rep,name=model,proto3" json:"model,omitempty" protobuf_key:"bytes,1,opt,name=key,proto3" protobuf_val:"bytes,2,opt,name=value,proto3"`
	ServiceId            string                    `protobuf:"bytes,4,opt,name=serviceId,proto3" json:"serviceId,omitempty"`
	TableName            string                    `protobuf:"bytes,5,opt,name=tableName,proto3" json:"tableName,omitempty"`
	Namespace            string                    `protobuf:"bytes,6,opt,name=namespace,proto3" json:"namespace,omitempty"`
	LoadType             string                    `protobuf:"bytes,7,opt,name=loadType,proto3" json:"loadType,omitempty"`
	FilePath             string                    `protobuf:"bytes,8,opt,name=filePath,proto3" json:"filePath,omitempty"`
	XXX_NoUnkeyedLiteral struct{}                  `json:"-"`
	XXX_unrecognized     []byte                    `json:"-"`
	XXX_sizecache        int32                     `json:"-"`
}

func (m *PublishRequest) Reset()         { *m = PublishRequest{} }
func (m *PublishRequest) String() string { return proto.CompactTextString(m) }
func (*PublishRequest) ProtoMessage()    {}
func (*PublishRequest) Descriptor() ([]byte, []int) {
	return fileDescriptor_b8ac95aa9096eaec, []int{12}
}

func (m *PublishRequest) XXX_Unmarshal(b []byte) error {
	return xxx_messageInfo_PublishRequest.Unmarshal(m, b)
}
func (m *PublishRequest) XXX_Marshal(b []byte, deterministic bool) ([]byte, error) {
	return xxx_messageInfo_PublishRequest.Marshal(b, m, deterministic)
}
func (m *PublishRequest) XXX_Merge(src proto.Message) {
	xxx_messageInfo_PublishRequest.Merge(m, src)
}
func (m *PublishRequest) XXX_Size() int {
	return xxx_messageInfo_PublishRequest.Size(m)
}
func (m *PublishRequest) XXX_DiscardUnknown() {
	xxx_messageInfo_PublishRequest.DiscardUnknown(m)
}

var xxx_messageInfo_PublishRequest proto.InternalMessageInfo

func (m *PublishRequest) GetLocal() *LocalInfo {
	if m != nil {
		return m.Local
	}
	return nil
}

func (m *PublishRequest) GetRole() map[string]*Party {
	if m != nil {
		return m.Role
	}
	return nil
}

func (m *PublishRequest) GetModel() map[string]*RoleModelInfo {
	if m != nil {
		return m.Model
	}
	return nil
}

func (m *PublishRequest) GetServiceId() string {
	if m != nil {
		return m.ServiceId
	}
	return ""
}

func (m *PublishRequest) GetTableName() string {
	if m != nil {
		return m.TableName
	}
	return ""
}

func (m *PublishRequest) GetNamespace() string {
	if m != nil {
		return m.Namespace
	}
	return ""
}

func (m *PublishRequest) GetLoadType() string {
	if m != nil {
		return m.LoadType
	}
	return ""
}

func (m *PublishRequest) GetFilePath() string {
	if m != nil {
		return m.FilePath
	}
	return ""
}

type PublishResponse struct {
	StatusCode           int32    `protobuf:"varint,1,opt,name=statusCode,proto3" json:"statusCode,omitempty"`
	Message              string   `protobuf:"bytes,2,opt,name=message,proto3" json:"message,omitempty"`
	Error                string   `protobuf:"bytes,3,opt,name=error,proto3" json:"error,omitempty"`
	Data                 []byte   `protobuf:"bytes,4,opt,name=data,proto3" json:"data,omitempty"`
	XXX_NoUnkeyedLiteral struct{} `json:"-"`
	XXX_unrecognized     []byte   `json:"-"`
	XXX_sizecache        int32    `json:"-"`
}

func (m *PublishResponse) Reset()         { *m = PublishResponse{} }
func (m *PublishResponse) String() string { return proto.CompactTextString(m) }
func (*PublishResponse) ProtoMessage()    {}
func (*PublishResponse) Descriptor() ([]byte, []int) {
	return fileDescriptor_b8ac95aa9096eaec, []int{13}
}

func (m *PublishResponse) XXX_Unmarshal(b []byte) error {
	return xxx_messageInfo_PublishResponse.Unmarshal(m, b)
}
func (m *PublishResponse) XXX_Marshal(b []byte, deterministic bool) ([]byte, error) {
	return xxx_messageInfo_PublishResponse.Marshal(b, m, deterministic)
}
func (m *PublishResponse) XXX_Merge(src proto.Message) {
	xxx_messageInfo_PublishResponse.Merge(m, src)
}
func (m *PublishResponse) XXX_Size() int {
	return xxx_messageInfo_PublishResponse.Size(m)
}
func (m *PublishResponse) XXX_DiscardUnknown() {
	xxx_messageInfo_PublishResponse.DiscardUnknown(m)
}

var xxx_messageInfo_PublishResponse proto.InternalMessageInfo

func (m *PublishResponse) GetStatusCode() int32 {
	if m != nil {
		return m.StatusCode
	}
	return 0
}

func (m *PublishResponse) GetMessage() string {
	if m != nil {
		return m.Message
	}
	return ""
}

func (m *PublishResponse) GetError() string {
	if m != nil {
		return m.Error
	}
	return ""
}

func (m *PublishResponse) GetData() []byte {
	if m != nil {
		return m.Data
	}
	return nil
}

func init() {
	proto.RegisterType((*Party)(nil), "com.webank.ai.fate.api.mlmodel.manager.Party")
	proto.RegisterType((*LocalInfo)(nil), "com.webank.ai.fate.api.mlmodel.manager.LocalInfo")
	proto.RegisterType((*ModelInfo)(nil), "com.webank.ai.fate.api.mlmodel.manager.ModelInfo")
	proto.RegisterType((*RoleModelInfo)(nil), "com.webank.ai.fate.api.mlmodel.manager.RoleModelInfo")
	proto.RegisterMapType((map[string]*ModelInfo)(nil), "com.webank.ai.fate.api.mlmodel.manager.RoleModelInfo.RoleModelInfoEntry")
	proto.RegisterType((*UnloadRequest)(nil), "com.webank.ai.fate.api.mlmodel.manager.UnloadRequest")
	proto.RegisterType((*UnloadResponse)(nil), "com.webank.ai.fate.api.mlmodel.manager.UnloadResponse")
	proto.RegisterType((*UnbindRequest)(nil), "com.webank.ai.fate.api.mlmodel.manager.UnbindRequest")
	proto.RegisterType((*UnbindResponse)(nil), "com.webank.ai.fate.api.mlmodel.manager.UnbindResponse")
	proto.RegisterType((*QueryModelRequest)(nil), "com.webank.ai.fate.api.mlmodel.manager.QueryModelRequest")
	proto.RegisterType((*ModelBindInfo)(nil), "com.webank.ai.fate.api.mlmodel.manager.ModelBindInfo")
	proto.RegisterType((*ModelInfoEx)(nil), "com.webank.ai.fate.api.mlmodel.manager.ModelInfoEx")
	proto.RegisterType((*QueryModelResponse)(nil), "com.webank.ai.fate.api.mlmodel.manager.QueryModelResponse")
	proto.RegisterType((*PublishRequest)(nil), "com.webank.ai.fate.api.mlmodel.manager.PublishRequest")
	proto.RegisterMapType((map[string]*RoleModelInfo)(nil), "com.webank.ai.fate.api.mlmodel.manager.PublishRequest.ModelEntry")
	proto.RegisterMapType((map[string]*Party)(nil), "com.webank.ai.fate.api.mlmodel.manager.PublishRequest.RoleEntry")
	proto.RegisterType((*PublishResponse)(nil), "com.webank.ai.fate.api.mlmodel.manager.PublishResponse")
}

func init() { proto.RegisterFile("model_service.proto", fileDescriptor_b8ac95aa9096eaec) }

var fileDescriptor_b8ac95aa9096eaec = []byte{
	// 782 bytes of a gzipped FileDescriptorProto
	0x1f, 0x8b, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0xff, 0xbc, 0x56, 0xcb, 0x4e, 0x1b, 0x3d,
	0x14, 0xd6, 0x24, 0x99, 0x84, 0x9c, 0x10, 0xf8, 0x31, 0xff, 0x62, 0x34, 0xaa, 0x2a, 0x3a, 0x8b,
	0x8a, 0x4d, 0x47, 0x6a, 0x50, 0x69, 0x61, 0xd5, 0x82, 0x10, 0x4d, 0xa1, 0x6d, 0x3a, 0x50, 0x75,
	0x59, 0x39, 0x19, 0x07, 0x46, 0xcc, 0xd8, 0x61, 0x2e, 0x40, 0x36, 0x55, 0xa5, 0xae, 0xfa, 0x08,
	0x5d, 0xf4, 0x79, 0xfa, 0x40, 0xed, 0x03, 0x54, 0xb6, 0xe7, 0xe2, 0x01, 0x8a, 0x72, 0x91, 0xd8,
	0xf9, 0x1c, 0xfb, 0x7c, 0xe7, 0x3b, 0x17, 0x1f, 0x1b, 0x56, 0x03, 0xe6, 0x12, 0xff, 0x73, 0x44,
	0xc2, 0x0b, 0x6f, 0x40, 0xec, 0x51, 0xc8, 0x62, 0x86, 0x1e, 0x0f, 0x58, 0x60, 0x5f, 0x92, 0x3e,
	0xa6, 0x67, 0x36, 0xf6, 0xec, 0x21, 0x8e, 0x89, 0x8d, 0x47, 0x9e, 0x1d, 0xf8, 0xe2, 0xb4, 0x1d,
	0x60, 0x8a, 0x4f, 0x48, 0x68, 0x3d, 0x02, 0xbd, 0x87, 0xc3, 0x78, 0x8c, 0x0c, 0x68, 0x8c, 0xf8,
	0xa2, 0xeb, 0x1a, 0xda, 0x5a, 0x75, 0xbd, 0xe9, 0x64, 0xa2, 0xb5, 0x05, 0xcd, 0x43, 0x36, 0xc0,
	0x7e, 0x97, 0x0e, 0x19, 0x42, 0x50, 0x0b, 0x99, 0x4f, 0x0c, 0x6d, 0x4d, 0x5b, 0x6f, 0x3a, 0x62,
	0xad, 0x9a, 0x56, 0x84, 0x3a, 0x37, 0xdd, 0x87, 0xe6, 0x5b, 0xee, 0x4e, 0x98, 0x3e, 0x80, 0x66,
	0x8c, 0xfb, 0x3e, 0x79, 0x87, 0x83, 0xcc, 0xbe, 0x50, 0xf0, 0x5d, 0x8a, 0x03, 0x12, 0x8d, 0xf0,
	0x80, 0xa4, 0x30, 0x85, 0xc2, 0xfa, 0xa3, 0x41, 0xdb, 0x61, 0x3e, 0x29, 0xd0, 0x28, 0xb4, 0x43,
	0x55, 0x21, 0x58, 0xb7, 0x3a, 0xaf, 0xed, 0xc9, 0x02, 0xb7, 0x4b, 0x68, 0x65, 0x69, 0x8f, 0xc6,
	0xe1, 0xd8, 0x29, 0xc3, 0x9b, 0x11, 0xa0, 0x9b, 0x87, 0xd0, 0x7f, 0x50, 0x3d, 0x23, 0xe3, 0x34,
	0x1a, 0xbe, 0x44, 0xfb, 0xa0, 0x5f, 0x60, 0x3f, 0x91, 0x31, 0xb4, 0x3a, 0x4f, 0x27, 0xe5, 0x93,
	0x03, 0x3b, 0xd2, 0x7e, 0xbb, 0xf2, 0x42, 0xb3, 0x0e, 0xa0, 0xfd, 0x91, 0xfa, 0x0c, 0xbb, 0x0e,
	0x39, 0x4f, 0x48, 0x14, 0xcf, 0x95, 0xc3, 0x37, 0xb0, 0x94, 0x81, 0x45, 0x23, 0x46, 0x23, 0x82,
	0x1e, 0x02, 0x44, 0x31, 0x8e, 0x93, 0x68, 0x97, 0xb9, 0x12, 0x4e, 0x77, 0x14, 0x0d, 0x2f, 0x6c,
	0x40, 0xa2, 0x08, 0x9f, 0x64, 0x68, 0x99, 0x68, 0x9d, 0x71, 0x62, 0x7d, 0x8f, 0xe6, 0xc4, 0x38,
	0x94, 0x6c, 0xc0, 0xae, 0x1b, 0xa5, 0x1d, 0xa4, 0x68, 0xca, 0xc4, 0x2b, 0x77, 0x12, 0xaf, 0xde,
	0x4a, 0x5c, 0x3a, 0x9b, 0x9b, 0xf8, 0x2f, 0x0d, 0x56, 0x3e, 0x24, 0x24, 0x1c, 0x8b, 0x7c, 0x2b,
	0x69, 0xcd, 0xb9, 0x66, 0x69, 0xcd, 0x15, 0xf3, 0x70, 0xe7, 0x4c, 0xfb, 0xe4, 0xc4, 0xa3, 0x5d,
	0xea, 0x92, 0x2b, 0xa3, 0x26, 0x99, 0x16, 0x1a, 0x64, 0xc2, 0x02, 0xa1, 0xae, 0xdc, 0xd5, 0xc5,
	0x6e, 0x2e, 0x73, 0xe4, 0x73, 0x4e, 0xf5, 0x78, 0x3c, 0x22, 0x46, 0x5d, 0x6c, 0x16, 0x0a, 0x6b,
	0x19, 0xda, 0x22, 0x86, 0x1d, 0x8f, 0x9f, 0x1f, 0x32, 0xeb, 0x87, 0x06, 0xad, 0xa2, 0x3d, 0xaf,
	0xe6, 0xe9, 0x95, 0x6b, 0xe5, 0xac, 0xde, 0x28, 0xa7, 0x01, 0x8d, 0x01, 0xa3, 0x31, 0xa1, 0xb1,
	0x88, 0xa9, 0xe9, 0x64, 0x22, 0xfa, 0x1f, 0x74, 0x4f, 0x89, 0x46, 0x0a, 0xd6, 0x4f, 0x0d, 0x90,
	0x9a, 0xf6, 0xb4, 0x8e, 0x06, 0x34, 0x42, 0x12, 0x0f, 0x8a, 0x22, 0x66, 0xe2, 0xbf, 0x2b, 0x88,
	0x8e, 0x00, 0x82, 0x2c, 0x4a, 0x49, 0xad, 0xd5, 0xd9, 0x98, 0xfa, 0x96, 0xed, 0x5d, 0x39, 0x0a,
	0x8c, 0xf5, 0xbb, 0x06, 0x4b, 0xbd, 0xa4, 0xef, 0x7b, 0xd1, 0x69, 0xd6, 0x13, 0xfb, 0xa0, 0xfb,
	0x7c, 0xec, 0x09, 0x66, 0x53, 0x5c, 0xe4, 0x7c, 0x56, 0x3a, 0xd2, 0x1e, 0x1d, 0xa7, 0x23, 0xb3,
	0x22, 0xa8, 0xbe, 0x9c, 0x14, 0xa7, 0x4c, 0x47, 0x4c, 0x28, 0x39, 0x98, 0xe4, 0xd0, 0xfd, 0x04,
	0xba, 0x38, 0x9f, 0x66, 0xe0, 0xd5, 0x8c, 0xb0, 0x22, 0x21, 0x12, 0x57, 0xe2, 0x95, 0xef, 0x42,
	0xed, 0xce, 0xbb, 0xa0, 0xdf, 0xd9, 0x54, 0xf5, 0xeb, 0x4d, 0x65, 0xc2, 0x02, 0x1f, 0x3f, 0xa2,
	0x9d, 0x1b, 0x62, 0x33, 0x97, 0xf9, 0xde, 0xd0, 0xf3, 0x49, 0x0f, 0xc7, 0xa7, 0xc6, 0x82, 0xdc,
	0xcb, 0x64, 0x73, 0x08, 0xcd, 0x3c, 0xfa, 0x5b, 0x26, 0xee, 0x6e, 0x79, 0xe2, 0x3e, 0x99, 0x38,
	0x13, 0xfc, 0x91, 0x52, 0xa6, 0xad, 0xc9, 0x00, 0x8a, 0x74, 0xdc, 0xe2, 0xe8, 0xa0, 0xec, 0xe8,
	0xd9, 0x4c, 0x4f, 0x8d, 0x3a, 0xde, 0x13, 0x58, 0xce, 0xcb, 0x31, 0xef, 0x64, 0xe3, 0x17, 0x8f,
	0x84, 0x21, 0x0b, 0xd3, 0x19, 0x24, 0x05, 0xfe, 0x5e, 0xbb, 0x38, 0xc6, 0xa2, 0x90, 0x8b, 0x8e,
	0x58, 0x77, 0xbe, 0xd7, 0x61, 0x51, 0xf0, 0x39, 0x92, 0x65, 0x45, 0x5f, 0xa0, 0x35, 0x92, 0x3c,
	0x0e, 0x19, 0x76, 0xd1, 0xe6, 0x6c, 0xbd, 0x64, 0x3e, 0x9f, 0xda, 0x2e, 0x0d, 0xba, 0xf0, 0xcf,
	0x87, 0xd9, 0xfd, 0xfb, 0xff, 0xaa, 0x41, 0x3b, 0x25, 0xf0, 0x9e, 0xfa, 0x1e, 0x25, 0xf7, 0x4f,
	0xe1, 0x9b, 0x06, 0x70, 0x9e, 0x0f, 0x48, 0xb4, 0x35, 0x29, 0xce, 0x8d, 0xb7, 0xcc, 0xdc, 0x9e,
	0xc5, 0x34, 0x65, 0x71, 0x09, 0xf5, 0x44, 0x7c, 0x11, 0xd0, 0xc4, 0xcd, 0x5d, 0xfa, 0x9f, 0x98,
	0x9b, 0xd3, 0x9a, 0xa9, 0x8e, 0xf9, 0x13, 0x3f, 0x8d, 0x63, 0xe5, 0xff, 0x31, 0x8d, 0x63, 0xf5,
	0x27, 0xb1, 0xb3, 0xba, 0xb3, 0xa2, 0x5e, 0x85, 0x1e, 0xff, 0x3c, 0xf7, 0xeb, 0xe2, 0x0f, 0xbd,
	0xf1, 0x37, 0x00, 0x00, 0xff, 0xff, 0xf9, 0x29, 0x77, 0x82, 0x5a, 0x0b, 0x00, 0x00,
}

// Reference imports to suppress errors if they are not otherwise used.
var _ context.Context
var _ grpc.ClientConn

// This is a compile-time assertion to ensure that this generated file
// is compatible with the grpc package it is being compiled against.
const _ = grpc.SupportPackageIsVersion4

// ModelServiceClient is the client API for ModelService service.
//
// For semantics around ctx use and closing/ending streaming RPCs, please refer to https://godoc.org/google.golang.org/grpc#ClientConn.NewStream.
type ModelServiceClient interface {
	PublishLoad(ctx context.Context, in *PublishRequest, opts ...grpc.CallOption) (*PublishResponse, error)
	PublishBind(ctx context.Context, in *PublishRequest, opts ...grpc.CallOption) (*PublishResponse, error)
	PublishOnline(ctx context.Context, in *PublishRequest, opts ...grpc.CallOption) (*PublishResponse, error)
	QueryModel(ctx context.Context, in *QueryModelRequest, opts ...grpc.CallOption) (*QueryModelResponse, error)
	Unload(ctx context.Context, in *UnloadRequest, opts ...grpc.CallOption) (*UnloadResponse, error)
	Unbind(ctx context.Context, in *UnbindRequest, opts ...grpc.CallOption) (*UnbindResponse, error)
}

type modelServiceClient struct {
	cc *grpc.ClientConn
}

func NewModelServiceClient(cc *grpc.ClientConn) ModelServiceClient {
	return &modelServiceClient{cc}
}

func (c *modelServiceClient) PublishLoad(ctx context.Context, in *PublishRequest, opts ...grpc.CallOption) (*PublishResponse, error) {
	out := new(PublishResponse)
	err := c.cc.Invoke(ctx, "/com.webank.ai.fate.api.mlmodel.manager.ModelService/publishLoad", in, out, opts...)
	if err != nil {
		return nil, err
	}
	return out, nil
}

func (c *modelServiceClient) PublishBind(ctx context.Context, in *PublishRequest, opts ...grpc.CallOption) (*PublishResponse, error) {
	out := new(PublishResponse)
	err := c.cc.Invoke(ctx, "/com.webank.ai.fate.api.mlmodel.manager.ModelService/publishBind", in, out, opts...)
	if err != nil {
		return nil, err
	}
	return out, nil
}

func (c *modelServiceClient) PublishOnline(ctx context.Context, in *PublishRequest, opts ...grpc.CallOption) (*PublishResponse, error) {
	out := new(PublishResponse)
	err := c.cc.Invoke(ctx, "/com.webank.ai.fate.api.mlmodel.manager.ModelService/publishOnline", in, out, opts...)
	if err != nil {
		return nil, err
	}
	return out, nil
}

func (c *modelServiceClient) QueryModel(ctx context.Context, in *QueryModelRequest, opts ...grpc.CallOption) (*QueryModelResponse, error) {
	out := new(QueryModelResponse)
	err := c.cc.Invoke(ctx, "/com.webank.ai.fate.api.mlmodel.manager.ModelService/queryModel", in, out, opts...)
	if err != nil {
		return nil, err
	}
	return out, nil
}

func (c *modelServiceClient) Unload(ctx context.Context, in *UnloadRequest, opts ...grpc.CallOption) (*UnloadResponse, error) {
	out := new(UnloadResponse)
	err := c.cc.Invoke(ctx, "/com.webank.ai.fate.api.mlmodel.manager.ModelService/unload", in, out, opts...)
	if err != nil {
		return nil, err
	}
	return out, nil
}

func (c *modelServiceClient) Unbind(ctx context.Context, in *UnbindRequest, opts ...grpc.CallOption) (*UnbindResponse, error) {
	out := new(UnbindResponse)
	err := c.cc.Invoke(ctx, "/com.webank.ai.fate.api.mlmodel.manager.ModelService/unbind", in, out, opts...)
	if err != nil {
		return nil, err
	}
	return out, nil
}

// ModelServiceServer is the server API for ModelService service.
type ModelServiceServer interface {
	PublishLoad(context.Context, *PublishRequest) (*PublishResponse, error)
	PublishBind(context.Context, *PublishRequest) (*PublishResponse, error)
	PublishOnline(context.Context, *PublishRequest) (*PublishResponse, error)
	QueryModel(context.Context, *QueryModelRequest) (*QueryModelResponse, error)
	Unload(context.Context, *UnloadRequest) (*UnloadResponse, error)
	Unbind(context.Context, *UnbindRequest) (*UnbindResponse, error)
}

// UnimplementedModelServiceServer can be embedded to have forward compatible implementations.
type UnimplementedModelServiceServer struct {
}

func (*UnimplementedModelServiceServer) PublishLoad(ctx context.Context, req *PublishRequest) (*PublishResponse, error) {
	return nil, status.Errorf(codes.Unimplemented, "method PublishLoad not implemented")
}
func (*UnimplementedModelServiceServer) PublishBind(ctx context.Context, req *PublishRequest) (*PublishResponse, error) {
	return nil, status.Errorf(codes.Unimplemented, "method PublishBind not implemented")
}
func (*UnimplementedModelServiceServer) PublishOnline(ctx context.Context, req *PublishRequest) (*PublishResponse, error) {
	return nil, status.Errorf(codes.Unimplemented, "method PublishOnline not implemented")
}
func (*UnimplementedModelServiceServer) QueryModel(ctx context.Context, req *QueryModelRequest) (*QueryModelResponse, error) {
	return nil, status.Errorf(codes.Unimplemented, "method QueryModel not implemented")
}
func (*UnimplementedModelServiceServer) Unload(ctx context.Context, req *UnloadRequest) (*UnloadResponse, error) {
	return nil, status.Errorf(codes.Unimplemented, "method Unload not implemented")
}
func (*UnimplementedModelServiceServer) Unbind(ctx context.Context, req *UnbindRequest) (*UnbindResponse, error) {
	return nil, status.Errorf(codes.Unimplemented, "method Unbind not implemented")
}

func RegisterModelServiceServer(s *grpc.Server, srv ModelServiceServer) {
	s.RegisterService(&_ModelService_serviceDesc, srv)
}

func _ModelService_PublishLoad_Handler(srv interface{}, ctx context.Context, dec func(interface{}) error, interceptor grpc.UnaryServerInterceptor) (interface{}, error) {
	in := new(PublishRequest)
	if err := dec(in); err != nil {
		return nil, err
	}
	if interceptor == nil {
		return srv.(ModelServiceServer).PublishLoad(ctx, in)
	}
	info := &grpc.UnaryServerInfo{
		Server:     srv,
		FullMethod: "/com.webank.ai.fate.api.mlmodel.manager.ModelService/PublishLoad",
	}
	handler := func(ctx context.Context, req interface{}) (interface{}, error) {
		return srv.(ModelServiceServer).PublishLoad(ctx, req.(*PublishRequest))
	}
	return interceptor(ctx, in, info, handler)
}

func _ModelService_PublishBind_Handler(srv interface{}, ctx context.Context, dec func(interface{}) error, interceptor grpc.UnaryServerInterceptor) (interface{}, error) {
	in := new(PublishRequest)
	if err := dec(in); err != nil {
		return nil, err
	}
	if interceptor == nil {
		return srv.(ModelServiceServer).PublishBind(ctx, in)
	}
	info := &grpc.UnaryServerInfo{
		Server:     srv,
		FullMethod: "/com.webank.ai.fate.api.mlmodel.manager.ModelService/PublishBind",
	}
	handler := func(ctx context.Context, req interface{}) (interface{}, error) {
		return srv.(ModelServiceServer).PublishBind(ctx, req.(*PublishRequest))
	}
	return interceptor(ctx, in, info, handler)
}

func _ModelService_PublishOnline_Handler(srv interface{}, ctx context.Context, dec func(interface{}) error, interceptor grpc.UnaryServerInterceptor) (interface{}, error) {
	in := new(PublishRequest)
	if err := dec(in); err != nil {
		return nil, err
	}
	if interceptor == nil {
		return srv.(ModelServiceServer).PublishOnline(ctx, in)
	}
	info := &grpc.UnaryServerInfo{
		Server:     srv,
		FullMethod: "/com.webank.ai.fate.api.mlmodel.manager.ModelService/PublishOnline",
	}
	handler := func(ctx context.Context, req interface{}) (interface{}, error) {
		return srv.(ModelServiceServer).PublishOnline(ctx, req.(*PublishRequest))
	}
	return interceptor(ctx, in, info, handler)
}

func _ModelService_QueryModel_Handler(srv interface{}, ctx context.Context, dec func(interface{}) error, interceptor grpc.UnaryServerInterceptor) (interface{}, error) {
	in := new(QueryModelRequest)
	if err := dec(in); err != nil {
		return nil, err
	}
	if interceptor == nil {
		return srv.(ModelServiceServer).QueryModel(ctx, in)
	}
	info := &grpc.UnaryServerInfo{
		Server:     srv,
		FullMethod: "/com.webank.ai.fate.api.mlmodel.manager.ModelService/QueryModel",
	}
	handler := func(ctx context.Context, req interface{}) (interface{}, error) {
		return srv.(ModelServiceServer).QueryModel(ctx, req.(*QueryModelRequest))
	}
	return interceptor(ctx, in, info, handler)
}

func _ModelService_Unload_Handler(srv interface{}, ctx context.Context, dec func(interface{}) error, interceptor grpc.UnaryServerInterceptor) (interface{}, error) {
	in := new(UnloadRequest)
	if err := dec(in); err != nil {
		return nil, err
	}
	if interceptor == nil {
		return srv.(ModelServiceServer).Unload(ctx, in)
	}
	info := &grpc.UnaryServerInfo{
		Server:     srv,
		FullMethod: "/com.webank.ai.fate.api.mlmodel.manager.ModelService/Unload",
	}
	handler := func(ctx context.Context, req interface{}) (interface{}, error) {
		return srv.(ModelServiceServer).Unload(ctx, req.(*UnloadRequest))
	}
	return interceptor(ctx, in, info, handler)
}

func _ModelService_Unbind_Handler(srv interface{}, ctx context.Context, dec func(interface{}) error, interceptor grpc.UnaryServerInterceptor) (interface{}, error) {
	in := new(UnbindRequest)
	if err := dec(in); err != nil {
		return nil, err
	}
	if interceptor == nil {
		return srv.(ModelServiceServer).Unbind(ctx, in)
	}
	info := &grpc.UnaryServerInfo{
		Server:     srv,
		FullMethod: "/com.webank.ai.fate.api.mlmodel.manager.ModelService/Unbind",
	}
	handler := func(ctx context.Context, req interface{}) (interface{}, error) {
		return srv.(ModelServiceServer).Unbind(ctx, req.(*UnbindRequest))
	}
	return interceptor(ctx, in, info, handler)
}

var _ModelService_serviceDesc = grpc.ServiceDesc{
	ServiceName: "com.webank.ai.fate.api.mlmodel.manager.ModelService",
	HandlerType: (*ModelServiceServer)(nil),
	Methods: []grpc.MethodDesc{
		{
			MethodName: "publishLoad",
			Handler:    _ModelService_PublishLoad_Handler,
		},
		{
			MethodName: "publishBind",
			Handler:    _ModelService_PublishBind_Handler,
		},
		{
			MethodName: "publishOnline",
			Handler:    _ModelService_PublishOnline_Handler,
		},
		{
			MethodName: "queryModel",
			Handler:    _ModelService_QueryModel_Handler,
		},
		{
			MethodName: "unload",
			Handler:    _ModelService_Unload_Handler,
		},
		{
			MethodName: "unbind",
			Handler:    _ModelService_Unbind_Handler,
		},
	},
	Streams:  []grpc.StreamDesc{},
	Metadata: "model_service.proto",
}
