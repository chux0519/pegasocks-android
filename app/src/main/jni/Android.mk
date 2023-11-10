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

########################################################
## hev-socks5-tunnel
########################################################
include $(call all-subdir-makefiles)
include $(CLEAR_VARS)

LOCAL_PATH := $(ROOT_PATH)
GIT_VERSION = $(shell cd $(LOCAL_PATH)/pegasocks && git rev-parse --short HEAD)
PGS_VERSION := "\"v0.0.0-$(GIT_VERSION)\""

########################################################
## libevent
########################################################
# event_openssl
include $(CLEAR_VARS)

LIBEVENT_SOURCES_CORE := \
    buffer.c \
    bufferevent.c \
    bufferevent_filter.c \
    bufferevent_pair.c \
    bufferevent_ratelim.c \
    bufferevent_sock.c \
    bufferevent_openssl.c \
    bufferevent_ssl.c \
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
    epoll.c \
    epoll_sub.c \
    poll.c \
    select.c \
    event_tagging.c \
	http.c \
	evdns.c \
	evrpc.c

LOCAL_MODULE := event_openssl
LOCAL_STATIC_LIBRARIES := ssl crypto
LOCAL_SRC_FILES := $(addprefix libevent/, $(LIBEVENT_SOURCES_CORE))
LOCAL_CFLAGS := -I$(ROOT_PATH)/libevent \
	-I$(ROOT_PATH)/libevent/include \
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
    src/session/session.c \
    src/session/filter.c \
    src/server/helper.c \
    src/server/local.c \
    src/server/manager.c \
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

LOCAL_STATIC_LIBRARIES := event_openssl ssl crypto pcre
LOCAL_MODULE := pegas
LOCAL_SRC_FILES := $(addprefix pegasocks/, $(PEGAS_SOURCES))

LOCAL_CFLAGS := -I$(ROOT_PATH)/pegasocks/include \
	-I$(ROOT_PATH)/pegasocks/include/pegasocks \
	-I$(ROOT_PATH)/pegasocks/3rd-party \
	-I$(ROOT_PATH)/libevent/include \
	-I$(ROOT_PATH)/pegasocks/3rd-party/libcork/include \
	-I$(ROOT_PATH)/pegasocks/3rd-party/ipset/include \
	-I$(ROOT_PATH)/pcre \
	-DWITH_ACL=ON \
	-DPGS_VERSION=$(PGS_VERSION)


include $(BUILD_STATIC_LIBRARY)

#########################################################
### pcre
#########################################################

include $(CLEAR_VARS)

LOCAL_MODULE := pcre

LOCAL_CFLAGS += -DHAVE_CONFIG_H

LOCAL_C_INCLUDES := $(ROOT_PATH)/pcre/dist $(ROOT_PATH)/pcre

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
## native libs
########################################################

include $(CLEAR_VARS)

LOCAL_MODULE:= native-libs

LOCAL_C_INCLUDES:= $(ROOT_PATH)/pegasocks/include

LOCAL_SRC_FILES:= native-libs.cpp

LOCAL_LDLIBS := -ldl -llog

LOCAL_STATIC_LIBRARIES := libpegas

include $(BUILD_SHARED_LIBRARY)

########################################################
## prebuilt (openssl)
########################################################
include $(CLEAR_VARS)
LOCAL_MODULE := ssl
LOCAL_SRC_FILES := $(ROOT_PATH)/prebuilt/openssl/$(TARGET_ARCH_ABI)/lib/libssl.a
LOCAL_EXPORT_C_INCLUDES := $(ROOT_PATH)/prebuilt/openssl/$(TARGET_ARCH_ABI)/include
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := crypto
LOCAL_SRC_FILES := $(ROOT_PATH)/prebuilt/openssl/$(TARGET_ARCH_ABI)/lib/libcrypto.a
LOCAL_EXPORT_C_INCLUDES := $(ROOT_PATH)/prebuilt/openssl/$(TARGET_ARCH_ABI)/include
include $(PREBUILT_STATIC_LIBRARY)
