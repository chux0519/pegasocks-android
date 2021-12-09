# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
#
LOCAL_PATH := $(call my-dir)
ROOT_PATH := $(LOCAL_PATH)
GIT_VERSION = $(shell cd $(LOCAL_PATH)/pegasocks && git rev-parse --short HEAD)
PGS_VERSION := "\"v0.0.0-$(GIT_VERSION)\""

BUILD_SHARED_EXECUTABLE := $(LOCAL_PATH)/build-shared-executable.mk

########################################################
## libevent
########################################################
# event_core
include $(CLEAR_VARS)

LIBEVENT_SOURCES_CORE := \
    buffer.c \
    bufferevent.c \
    bufferevent_filter.c \
    bufferevent_pair.c \
    bufferevent_ratelim.c \
    bufferevent_sock.c \
    event.c \
    evmap.c \
    evthread.c \
    evutil.c \
    evutil_rand.c \
    evutil_time.c \
    listener.c \
    log.c \
    signal.c \
    strlcpy.c \
    epoll.c poll.c select.c \
    epoll_sub.c

LOCAL_MODULE := event_core
LOCAL_SRC_FILES := $(addprefix libevent/, $(LIBEVENT_SOURCES_CORE))
LOCAL_CFLAGS := -I$(LOCAL_PATH)/libevent \
	-I$(LOCAL_PATH)/libevent/include

include $(BUILD_STATIC_LIBRARY)

# event_mbedtls
include $(CLEAR_VARS)

LIBEVENT_SOURCES_MBEDTLS := bufferevent_mbedtls.c bufferevent_ssl.c

LOCAL_STATIC_LIBRARIES := event_extra event_core mbedtls
LOCAL_MODULE := event_mbedtls
LOCAL_SRC_FILES := $(addprefix libevent/, $(LIBEVENT_SOURCES_MBEDTLS))
LOCAL_CFLAGS := -I$(LOCAL_PATH)/libevent \
	-I$(LOCAL_PATH)/libevent/include \
	-I$(LOCAL_PATH)/mbedtls/include
include $(BUILD_STATIC_LIBRARY)


# event_extra
include $(CLEAR_VARS)

LIBEVENT_SOURCES_EXTRA := \
    event_tagging.c \
    http.c \
    evdns.c \
    evrpc.c

LOCAL_STATIC_LIBRARIES := event_core
LOCAL_MODULE := event_extra
LOCAL_SRC_FILES := $(addprefix libevent/, $(LIBEVENT_SOURCES_EXTRA))
LOCAL_CFLAGS := -I$(LOCAL_PATH)/libevent \
	-I$(LOCAL_PATH)/libevent/include \
	-DEVENT__DISABLE_THREAD_SUPPORT=1

include $(BUILD_STATIC_LIBRARY)

########################################################
## pegas
########################################################
include $(CLEAR_VARS)
PEGAS_SOURCES :=  \
    src/pegas.c \
    src/config.c \
    src/mpsc.c \
    src/log.c \
    src/utils.c \
    src/codec/websocket.c \
    src/codec/trojan.c \
    src/codec/vmess.c \
    src/codec/shadowsocks.c \
    src/session/session.c \
    src/session/inbound.c \
    src/session/outbound.c \
    src/server/helper.c \
    src/server/local.c \
    src/server/manager.c \
    src/server/metrics.c \
    src/server/control.c \
    src/ssl/mbedtls.c \
    src/crypto/mbedtls.c \
    3rd-party/parson/parson.c \
    3rd-party/hash_32a.c \
    3rd-party/sha3.c \
    src/acl.c \
    3rd-party/libcork/src/libcork/ds/array.c \
    3rd-party/libcork/src/libcork/ds/buffer.c \
    3rd-party/libcork/src/libcork/ds/managed-buffer.c \
    3rd-party/libcork/src/libcork/ds/slice.c \
    3rd-party/libcork/src/libcork/posix/process.c \
    3rd-party/libcork/src/libcork/ds/hash-table.c \
    3rd-party/libcork/src/libcork/core/hash.c \
    3rd-party/libcork/src/libcork/ds/dllist.c \
    3rd-party/libcork/src/libcork/core/allocator.c \
    3rd-party/libcork/src/libcork/core/error.c \
    3rd-party/libcork/src/libcork/core/ip-address.c \
    3rd-party/ipset/src/libipset/general.c \
    3rd-party/ipset/src/libipset/bdd/assignments.c \
    3rd-party/ipset/src/libipset/bdd/basics.c \
    3rd-party/ipset/src/libipset/bdd/bdd-iterator.c \
    3rd-party/ipset/src/libipset/bdd/expanded.c \
    3rd-party/ipset/src/libipset/bdd/reachable.c \
    3rd-party/ipset/src/libipset/set/allocation.c \
    3rd-party/ipset/src/libipset/set/inspection.c \
    3rd-party/ipset/src/libipset/set/ipv4_set.c \
    3rd-party/ipset/src/libipset/set/ipv6_set.c \
    3rd-party/ipset/src/libipset/set/iterator.c

LOCAL_STATIC_LIBRARIES := event_core event_extra event_mbedtls mbedtls pcre
LOCAL_MODULE := pegas
LOCAL_SRC_FILES := $(addprefix pegasocks/, $(PEGAS_SOURCES))

LOCAL_CFLAGS := -I$(LOCAL_PATH)/pegasocks/include \
	-I$(LOCAL_PATH)/pegasocks/include/pegasocks \
	-I$(LOCAL_PATH)/pegasocks/3rd-party \
	-I$(LOCAL_PATH)/libevent/include \
	-I$(LOCAL_PATH)/pegasocks/3rd-party/libcork/include \
	-I$(LOCAL_PATH)/pegasocks/3rd-party/ipset/include \
	-I$(LOCAL_PATH)/pcre \
	-I$(LOCAL_PATH)/mbedtls/include \
	-DWITH_ACL=ON \
	-DUSE_MBEDTLS=ON \
	-DPGS_VERSION=$(PGS_VERSION)


include $(BUILD_STATIC_LIBRARY)


########################################################
## tun2socks
########################################################

include $(CLEAR_VARS)

LOCAL_CFLAGS := -std=gnu99
LOCAL_CFLAGS += -DBADVPN_THREADWORK_USE_PTHREAD -DBADVPN_LINUX -DBADVPN_BREACTOR_BADVPN -D_GNU_SOURCE
LOCAL_CFLAGS += -DBADVPN_USE_SELFPIPE -DBADVPN_USE_EPOLL
LOCAL_CFLAGS += -DBADVPN_LITTLE_ENDIAN -DBADVPN_THREAD_SAFE
LOCAL_CFLAGS += -DNDEBUG -DANDROID

LOCAL_C_INCLUDES:= \
        $(LOCAL_PATH)/badvpn/lwip/src/include/ipv4 \
        $(LOCAL_PATH)/badvpn/lwip/src/include/ipv6 \
        $(LOCAL_PATH)/badvpn/lwip/src/include \
        $(LOCAL_PATH)/badvpn/lwip/custom \
        $(LOCAL_PATH)/badvpn/

TUN2SOCKS_SOURCES := \
        base/BLog_syslog.c \
        system/BReactor_badvpn.c \
        system/BSignal.c \
        system/BConnection_unix.c \
        system/BConnection_common.c \
        system/BTime.c \
        system/BUnixSignal.c \
        system/BNetwork.c \
        system/BDatagram_common.c \
        system/BDatagram_unix.c \
        flow/StreamRecvInterface.c \
        flow/PacketRecvInterface.c \
        flow/PacketPassInterface.c \
        flow/StreamPassInterface.c \
        flow/SinglePacketBuffer.c \
        flow/BufferWriter.c \
        flow/PacketBuffer.c \
        flow/PacketStreamSender.c \
        flow/PacketPassConnector.c \
        flow/PacketProtoFlow.c \
        flow/PacketPassFairQueue.c \
        flow/PacketProtoEncoder.c \
        flow/PacketProtoDecoder.c \
        socksclient/BSocksClient.c \
        tuntap/BTap.c \
        lwip/src/core/udp.c \
        lwip/src/core/memp.c \
        lwip/src/core/init.c \
        lwip/src/core/pbuf.c \
        lwip/src/core/tcp.c \
        lwip/src/core/tcp_out.c \
        lwip/src/core/sys.c \
        lwip/src/core/netif.c \
        lwip/src/core/def.c \
        lwip/src/core/mem.c \
        lwip/src/core/tcp_in.c \
        lwip/src/core/stats.c \
        lwip/src/core/ip.c \
        lwip/src/core/timeouts.c \
        lwip/src/core/inet_chksum.c \
        lwip/src/core/ipv4/icmp.c \
        lwip/src/core/ipv4/ip4.c \
        lwip/src/core/ipv4/ip4_addr.c \
        lwip/src/core/ipv4/ip4_frag.c \
        lwip/src/core/ipv6/ip6.c \
        lwip/src/core/ipv6/nd6.c \
        lwip/src/core/ipv6/icmp6.c \
        lwip/src/core/ipv6/ip6_addr.c \
        lwip/src/core/ipv6/ip6_frag.c \
        lwip/custom/sys.c \
        tun2socks/tun2socks.c \
        base/DebugObject.c \
        base/BLog.c \
        base/BPending.c \
        flowextra/PacketPassInactivityMonitor.c \
        tun2socks/SocksUdpGwClient.c \
        udpgw_client/UdpGwClient.c \
        socks_udp_client/SocksUdpClient.c \

LOCAL_MODULE := tun2socks

LOCAL_LDLIBS := -ldl -llog

LOCAL_SRC_FILES := $(addprefix badvpn/, $(TUN2SOCKS_SOURCES))

include $(BUILD_STATIC_LIBRARY)

########################################################
## pcre 
########################################################

include $(CLEAR_VARS)

LOCAL_MODULE := pcre

LOCAL_CFLAGS += -DHAVE_CONFIG_H

LOCAL_C_INCLUDES := $(LOCAL_PATH)/pcre/dist $(LOCAL_PATH)/pcre

libpcre_src_files := \
    pcre_chartables.c \
    dist/pcre_byte_order.c \
    dist/pcre_compile.c \
    dist/pcre_config.c \
    dist/pcre_dfa_exec.c \
    dist/pcre_exec.c \
    dist/pcre_fullinfo.c \
    dist/pcre_get.c \
    dist/pcre_globals.c \
    dist/pcre_jit_compile.c \
    dist/pcre_maketables.c \
    dist/pcre_newline.c \
    dist/pcre_ord2utf8.c \
    dist/pcre_refcount.c \
    dist/pcre_string_utils.c \
    dist/pcre_study.c \
    dist/pcre_tables.c \
    dist/pcre_ucd.c \
    dist/pcre_valid_utf8.c \
    dist/pcre_version.c \
    dist/pcre_xclass.c

LOCAL_SRC_FILES := $(addprefix pcre/, $(libpcre_src_files))

include $(BUILD_STATIC_LIBRARY)

########################################################
## mbed TLS
########################################################

include $(CLEAR_VARS)

LOCAL_MODULE := mbedtls

LOCAL_C_INCLUDES := $(LOCAL_PATH)/mbedtls/include

MBEDTLS_SOURCES := $(wildcard $(LOCAL_PATH)/mbedtls/library/*.c)

LOCAL_SRC_FILES := $(MBEDTLS_SOURCES:$(LOCAL_PATH)/%=%)

include $(BUILD_STATIC_LIBRARY)


########################################################
## native libs
########################################################

include $(CLEAR_VARS)

LOCAL_MODULE:= native-libs

LOCAL_C_INCLUDES:= $(LOCAL_PATH)/pegasocks/include $(LOCAL_PATH)/badvpn

LOCAL_SRC_FILES:= native-libs.cpp

LOCAL_LDLIBS := -ldl -llog

LOCAL_STATIC_LIBRARIES := libpegas libtun2socks

include $(BUILD_SHARED_LIBRARY)



### openssl from prefab
$(call import-module, prefab/openssl)
