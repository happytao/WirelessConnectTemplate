package com.example.othertemplateapp.stream;

import java.io.IOException;
import java.io.InputStream;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ByteInputStream extends InputStream {
    private final Queue<byte[]> mQueue;
    private int index = 0;
    private volatile boolean isClose = false;

    public ByteInputStream() {
        mQueue = new ConcurrentLinkedDeque<>();
    }

    public void addData(byte[] data) {
        //Log.e("ByteInputStream", "addData: "+Util.byte2HexStr(data) );
        mQueue.offer(data);
    }

    public void open() {
        isClose = false;
    }

    @Override
    public int read() throws IOException {
        return getByteData(1)[0];
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (isClose)
            throw new IOException("连接已关闭");

        byte[] data = getByteData(len);
        System.arraycopy(data, 0, b, off, data.length);
        return data.length;
    }

    @Override
    public int available() {
        int sum = 0;
        for (byte[] bytes : mQueue) {
            sum += bytes.length;
        }
        return sum;
    }

    @Override
    public void close() {
        isClose = true;
        mQueue.clear();
    }

    private byte[] getByteData(int len) {
        int sum = Math.min(available(), len);
        int resultIndex = 0;

        byte[] result = new byte[sum];

        do {
            byte[] temp = mQueue.peek();

            if (temp == null)
                break;

            int tempLen = temp.length - index;

            if (sum >= tempLen + resultIndex) {
                mQueue.poll();
                System.arraycopy(temp, index, result, resultIndex, tempLen);
                resultIndex += tempLen;
                index = 0;
            } else {
                System.arraycopy(temp, index, result, resultIndex, sum - resultIndex);
                index = index + sum - resultIndex;
                break;
            }
        } while (true);

        return result;
    }

    /*测试用代码*/
//    @Test
//    public void test() {
//        ByteInputStream inputStream = new ByteInputStream();
//        inputStream.addData(Util.hexString2Bytes("0200040A3015010329"));
//        byte[] data = new byte[1];
//        try {
//            inputStream.read(data);
//            System.out.println(Util.byte2HexStr(data, 0, data.length));
//            data = new byte[4];
//            inputStream.read(data, 0, 4);
//            System.out.println(Util.byte2HexStr(data, 0, data.length));
//            data = new byte[4];
//            inputStream.read(data, 0, 4);
//            System.out.println(Util.byte2HexStr(data, 0, data.length));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
