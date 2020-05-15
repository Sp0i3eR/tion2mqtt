package expert.serebro.tion.command;

import expert.serebro.tion.Util;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class AbstractChainedResponseBluetoothCommand<T, R> implements BluetoothCommand<T>, ResponseCommand<R> {
    private byte[] first;
    private byte[] middle;
    @Getter(value = AccessLevel.PROTECTED)
    private byte[] fullChain;
    private final CountDownLatch latch = new CountDownLatch(1);


    public static byte[] getFullCommand(byte[] first, byte[] middle, byte[] last) {
        int firstLength = first == null ? 0 : first.length;
        int middleLength = middle == null ? 0 : middle.length;
        int lastLength = last == null ? 0 : last.length;
        byte[] result = new byte[(firstLength + middleLength + lastLength)];
        if (firstLength > 0) {
            System.arraycopy(first, 0, result, 0, firstLength);
        }
        if (middleLength > 0) {
            System.arraycopy(middle, 0, result, firstLength, middleLength);
        }
        if (lastLength > 0) {
            System.arraycopy(last, 0, result, firstLength + middleLength, lastLength);
        }
        return result;
    }

    public static byte[] removeFrameIdFromPackage(byte[] bArr) {
        byte[] bArr2 = new byte[(bArr.length - 1)];
        System.arraycopy(bArr, 1, bArr2, 0, bArr.length - 1);
        return bArr2;
    }


    abstract long getResponseTimeout();

    @Override
    public void responseCallback(byte[] data) {
        if (data.length == 0) {
            log.warn("Dropping zero size chunk");
        } else if (data[0] == -128) { // SINGLE
            this.fullChain = data;
            log.debug(String.format("Got single frame %s", Util.hexEncode(data)));
            latch.countDown();
        } else if (data[0] == 0) { //FIRST
            this.first = data;
            this.middle = null;
            log.debug(String.format("Got first frame %s", Util.hexEncode(data)));
        } else if (data[0] == -64) {      // LAST
            this.fullChain = getFullCommand(this.first, this.middle, removeFrameIdFromPackage(data));
            log.debug(String.format("Got last frame %s", Util.hexEncode(data)));
            latch.countDown();
        } else { // MIDDLE
            byte[] dataWoFrameId = removeFrameIdFromPackage(data);
            int length = this.middle == null ? 0 : this.middle.length;
            byte[] newMiddle = new byte[(dataWoFrameId.length + length)];
            if (this.middle != null && this.middle.length > 0) {
                System.arraycopy(this.middle, 0, newMiddle, 0, this.middle.length);
            }
            System.arraycopy(dataWoFrameId, 0, newMiddle, length, dataWoFrameId.length);
            this.middle = newMiddle;
            log.debug(String.format("Got middle frame %s", Util.hexEncode(data)));
        }
    }


    @Override
    public boolean waitForResponse() {
        try {
            return latch.await(getResponseTimeout(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public void run() {
    }


}
