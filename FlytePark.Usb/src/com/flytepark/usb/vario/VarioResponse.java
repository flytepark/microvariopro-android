package com.flytepark.usb.vario;

public enum VarioResponse {
    REALTIME_DATA(0x00),
    OK(0x01),
    OPTIONS(0x02),
    OPTION(0x03),
    LOG(0x04),
    TRACK(0x05),
    ID(0x06),
    RTC(0x07),
    TRLOG_COUNT(0x08),
    BAD_MAGIC(0x09),
    BAD_NBYTES(0x0A),
    BAD_CHECKSUM(0x0B),
    BAD_COMMAND(0x0C),
    BAD_PARAMETER(0x0D),
    MISSED_PACKETS(0x0E),
    INITIALIZED(0x0F),
    TRACK_STATISTICS(0x10),
    TIMING(0xF);
    
    private final int value;    

    private VarioResponse(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }

}
