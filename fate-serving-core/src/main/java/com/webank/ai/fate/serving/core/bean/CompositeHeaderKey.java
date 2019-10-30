package com.webank.ai.fate.serving.core.bean;

import io.grpc.Metadata;

public  class CompositeHeaderKey {

        private final String keyString;
        private final Metadata.Key<String> stringMetaKey;
        private final Metadata.Key<byte[]> bytesMetaKey;
        private final io.grpc.Context.Key<String> stringContextKey;

        private CompositeHeaderKey(String key) {
            this.keyString = key;
            this.stringMetaKey = Metadata.Key.<String>of(keyString, Metadata.ASCII_STRING_MARSHALLER);
            this.bytesMetaKey = Metadata.Key.<byte[]>of(keyString + Metadata.BINARY_HEADER_SUFFIX, Metadata.BINARY_BYTE_MARSHALLER);
            this.stringContextKey = io.grpc.Context.<String>key(keyString);

        }

        public static CompositeHeaderKey from(String key) {
            return new CompositeHeaderKey(key);
        }


        public Metadata.Key<String> asMetaKey() {
            return stringMetaKey;
        }

        public Metadata.Key<byte[]> asMetaBytesKey() {
            return bytesMetaKey;
        }

        public io.grpc.Context.Key<String> asContextKey() {
            return stringContextKey;
        }


        public String asString() {
            return keyString;
        }

        @Override
        public String toString() {
            return keyString;
        }

    }