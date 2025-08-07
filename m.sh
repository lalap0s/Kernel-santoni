#!/bin/bash

#==============#
#  Variabel    #
#==============#
bold=$(tput bold)
normal=$(tput sgr0)
DEFCONFIG="4x_defconfig"
AK3_REPO="https://github.com/lalap0s/AnyKernel3.git"
AK3_DIR="AnyKernel3"
KDIR="$(pwd)"
NAME="Lalap0s"
DEVICE="santoni-susfs"
VERSION="4.9.309"
ZIP="${NAME}-${VERSION}-${DEVICE}.zip"
CLANG_PATH="/root/clang/proton-clang/bin"

export KBUILD_BUILD_HOST="server-build"    
export KBUILD_BUILD_USER="android"        
export PATH="${CLANG_PATH}:$PATH"
export KBUILD_BUILD_TIMESTAMP="Thu Sep 26 11:58:27 UTC 2024"
export USE_CCACHE=1
export CCACHE_DIR=~/.ccache
ccache -M 10G

#==================#
#   Fungsi-fungsi  #
#==================#

function print_header() {
    echo -e "==========================="
    echo -e "= $1"
    echo -e "==========================="
}

function clean_output() {
    print_header "Cleaning Output Directory"
    rm -rf out
}

function clone_anykernel3() {
    print_header "Cloning AnyKernel3"
    if [ -d "$AK3_DIR" ]; then
        echo "[!] $AK3_DIR already exists. Skipping..."
    else
        git clone "$AK3_REPO" || {
            echo "[✘] Failed to clone AnyKernel3"
            exit 1
        }
    fi
}

function regen_defconfig() {
    make O=out ARCH=arm64 "$DEFCONFIG" savedefconfig
    cp out/defconfig "arch/arm64/configs/${DEFCONFIG}"
    echo "[✓] Defconfig regenerated."
    exit 0
}

function compile_kernel() {
    print_header "Start Kernel Compilation"
    mkdir -p out
    make O=out ARCH=arm64 "$DEFCONFIG"

    make -j"$(nproc --all)" O=out \
        ARCH=arm64 \
        CC="ccache clang" \
        HOSTCC="ccache clang"  \
        AR=llvm-ar \
        AS=llvm-as \
        NM=llvm-nm \
        OBJDUMP=llvm-objdump \
        STRIP=llvm-strip \
        CROSS_COMPILE=aarch64-linux-gnu- \
        CROSS_COMPILE_ARM32=arm-linux-gnueabi- \
        LLVM=1 \
        2>&1 | tee log.txt

    print_header "Compile Kernel Complete"
}

function zip_kernel() {
    print_header "Start Zipping"

    if [ -f out/arch/arm64/boot/Image.gz-dtb ]; then
        cp out/arch/arm64/boot/Image.gz-dtb "$AK3_DIR"
        cd "$AK3_DIR" || exit

        zip -rq9 "${KDIR}/../${ZIP}" * -x "README.md" || {
            echo "[✘] Failed to create ZIP file."
            exit 1
        }

        cd "$KDIR" || exit
        echo "[✓] ZIP created: ${ZIP}"

        echo "[i] Link: $(curl --upload-file "${KDIR}/../${ZIP}" https://free.keep.sh)"
        echo "[i] MD5 : $(md5sum ../${ZIP} | cut -d' ' -f1)"

        print_header "Successfully Zipped"
    else
        echo "[✘] Image.gz-dtb not found!"
        exit 1
    fi
}

#==================#
#      MAIN        #
#==================#

print_header "LalapOs Kernel Build Script"

# Parsing opsi CLI
while (( $# )); do
    case $1 in
        -Z|--zip) MAKE_ZIP=true ;;
        -r|--regen) regen_defconfig ;;
        -c|--clean) clean_output ;;
        *) echo "[!] Unknown argument: $1" ;;
    esac
    shift
done

# Eksekusi
clean_output
clone_anykernel3
compile_kernel
$MAKE_ZIP && zip_kernel

exit 0
