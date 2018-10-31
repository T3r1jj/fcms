FROM circleci/openjdk:8-jdk-node-browsers

RUN sudo apt install -y autoconf libtool g++ libcrypto++-dev libz-dev libsqlite3-dev libssl-dev libcurl4-openssl-dev libreadline-dev libpcre++-dev libsodium-dev libc-ares-dev libfreeimage-dev libavcodec-dev libavutil-dev libavformat-dev libswscale-dev libmediainfo-dev libzen-dev
RUN sudo git clone https://github.com/meganz/MEGAcmd.git
WORKDIR "/MEGAcmd"
RUN sudo git submodule update --init --recursive
RUN sudo sh autogen.sh
RUN sudo ./configure
RUN sudo make
RUN sudo make install