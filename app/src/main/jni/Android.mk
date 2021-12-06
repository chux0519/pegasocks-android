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
    epoll.c poll.c select.c

LOCAL_MODULE := event_core
LOCAL_SRC_FILES := $(addprefix libevent/, $(LIBEVENT_SOURCES_CORE))
LOCAL_CFLAGS := -I$(LOCAL_PATH)/libevent \
	-I$(LOCAL_PATH)/libevent/include \

include $(BUILD_STATIC_LIBRARY)

# event_openssl
include $(CLEAR_VARS)

LIBEVENT_SOURCES_OPENSSL := bufferevent_openssl.c bufferevent_ssl.c

LOCAL_STATIC_LIBRARIES := event_core ssl crypto
LOCAL_MODULE := event_openssl
LOCAL_SRC_FILES := $(addprefix libevent/, $(LIBEVENT_SOURCES_OPENSSL))
LOCAL_CFLAGS := -I$(LOCAL_PATH)/libevent \
	-I$(LOCAL_PATH)/libevent/include \

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
    src/ssl/openssl.c \
    src/crypto/openssl.c \
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

LOCAL_STATIC_LIBRARIES := event_core event_extra event_openssl \
    ssl crypto libpcre
LOCAL_MODULE := pegas
LOCAL_SRC_FILES := $(addprefix pegasocks/, $(PEGAS_SOURCES))

LOCAL_CFLAGS := -I$(LOCAL_PATH)/pegasocks/include \
	-I$(LOCAL_PATH)/pegasocks/include/pegasocks \
	-I$(LOCAL_PATH)/pegasocks/3rd-party \
	-I$(LOCAL_PATH)/libevent/include \
	-I$(LOCAL_PATH)/pegasocks/3rd-party/libcork/include \
	-I$(LOCAL_PATH)/pegasocks/3rd-party/ipset/include \
	-I$(LOCAL_PATH)/pcre


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

### openssl from prefab
$(call import-module, prefab/openssl)
