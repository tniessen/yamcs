package org.yamcs.simulation.simulator.cfdp;

import java.nio.ByteBuffer;

public class EofPacket extends Packet {

    private Header header;
    private ByteBuffer buffer;
    private ConditionCode conditionCode;
    private long fileChecksum;
    private long fileSize;
    private byte[] faultLocation;

    public EofPacket(ByteBuffer buffer, Header header) {
        this.header = header;
        this.buffer = buffer;

        this.conditionCode = ConditionCode.readConditionCode(buffer);
        this.fileChecksum = Utils.getUnsignedInt(buffer);
        this.fileSize = Utils.getUnsignedShort(buffer);

        if (conditionCode != ConditionCode.NoError
                && conditionCode != ConditionCode.Reserved) {
            TLV temp = TLV.readTLV(buffer);
            if (temp.getType() == 0x06) {
                faultLocation = temp.getValue();
            } else {
                // TODO
            }
        }
    }

}