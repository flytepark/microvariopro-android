package com.flytepark.usb.vario;

public enum VarioCommand {
    INITIALIZE(0x00),
    REALTIME_DATA_MODE(0x01),
    READ_OPTIONS(0x02),
    READ_OPTION(0x03),
    WRITE_OPTIONS(0x04),
    WRITE_OPTION(0x05),
    READ_LOG(0x06),
    READ_TRACK(0x07),
    ERASE_TRACKS(0x08),
    ERASE_FLIGHTS(0x09),
    SET_FACTORY_DEFAULTS(0x0A),
    READ_ID(0x0B),
    READ_RTC(0x0C),
    WRITE_RTC(0x0D),
    READ_TRACK_LOG_COUNT(0x0E),
    READ_TRACK_STATISTICS(0x0F),
    FACTORY_TEST(0x10),
    END_REALTIME(0x11),
    SET_VARIO_AUDIO(0x12);
    //LAST(0x0E
    
    
    private final int value;    

    private VarioCommand(int value) {
      this.value=value;
    }

    public int getValue() {
      return value;
    }

}
