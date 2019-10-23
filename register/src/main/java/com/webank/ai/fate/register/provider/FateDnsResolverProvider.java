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

package com.webank.ai.fate.register.provider;//package com.webank.ai.fate.register.provider;
//
//import com.google.common.base.Preconditions;
//import com.google.common.base.Stopwatch;
//import io.grpc.InternalServiceProviders;
//import io.grpc.NameResolver;
//import io.grpc.NameResolverProvider;
//import io.grpc.internal.DnsNameResolver;
//import io.grpc.internal.GrpcUtil;
//
//import java.net.URI;
//
///**
// * @Description TODO
// * @Author kaideng
// **/
//public class FateDnsResolverProvider  extends NameResolverProvider {
//    private static final String SCHEME = "dns";
//
//    public FateDnsResolverProvider() {
//    }
//
//    @Override
//    public String getDefaultScheme() {
//        return null;
//    }
//
//    @Override
//    protected boolean isAvailable() {
//        return false;
//    }
//
//    @Override
//    protected int priority() {
//        return 0;
//    }
//
//    @Override
//    public DnsNameResolver newNameResolver(URI targetUri, NameResolver.Helper helper) {
//        if ("dns".equals(targetUri.getScheme())) {
//            String targetPath = (String) Preconditions.checkNotNull(targetUri.getPath(), "targetPath");
//            Preconditions.checkArgument(targetPath.startsWith("/"), "the path component (%s) of the target (%s) must start with '/'", targetPath, targetUri);
//            String name = targetPath.substring(1);
//            return new DnsNameResolver(targetUri.getAuthority(), name, helper, GrpcUtil.SHARED_CHANNEL_EXECUTOR, Stopwatch.createUnstarted(), InternalServiceProviders.isAndroid(this.getClass().getClassLoader()));
//        } else {
//            return null;
//        }
//    }
////
////    public String getDefaultScheme() {
////        return "dns";
////    }
////
////    protected boolean isAvailable() {
////        return true;
////    }
////
////    protected int priority() {
////        return 5;
////    }
//}
//
