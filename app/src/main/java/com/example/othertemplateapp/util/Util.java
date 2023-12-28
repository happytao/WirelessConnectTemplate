package com.example.othertemplateapp.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class Util {

    public static byte[] fillTail(byte[] bytes, byte fillByte, int fillCount) {
        byte[] newBytes = new byte[bytes.length + fillCount];
        System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
        for (int i = bytes.length; i < newBytes.length; i++) {
            newBytes[i] = fillByte;
        }
        return newBytes;
    }

    private static final int BUFF_SIZE = 1024 * 1024; // 1M Byte

    public static int byte2Int(byte[] byteArray) {
        int integer = 0x00;
        int shiftCount = 0;
        for (int i = byteArray.length - 1; i >= 0 && i >= byteArray.length - 4; ) {
            byte b = byteArray[i];
            // byte为负数左移后转为int还是负数
            integer |= ((b & 0xFF) << shiftCount);
            i--;
            shiftCount += 8;
        }
        return integer;
    }

    public static String bitmapToBase64(Bitmap bitmap) {

        //        Bitmap bitmap = Bitmap.createScaledBitmap(bmp,640,480,true);

        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                baos.flush();
                baos.close();
                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * 将int转为hex
     *
     * @param i
     * @param length 几位hex, 比如length为1的话15被转为F, 16被转为0
     * @return
     */
    public static String int2Hex(int i, int length) {
        if (length <= 0) {
            throw new RuntimeException("指定的字符串长度不支持");
        }
        long l;
        switch (length) {
            case 1:
                l = 0xFL;
                break;
            case 2:
                l = 0xFFL;
                break;
            case 3:
                l = 0xFFFL;
                break;
            case 4:
                l = 0xFFFFL;
                break;
            case 5:
                l = 0xFFFFFL;
                break;
            case 6:
                l = 0xFFFFFFL;
                break;
            case 7:
                l = 0xFFFFFFFL;
                break;
            default:
                l = 0xFFFFFFFFL;
                break;
        }
        String result = Long.toHexString(l & i);
        StringBuilder builder = new StringBuilder(result);
        for (int j = 0; j < length - result.length(); j++) {
            builder.insert(0, "0");
        }
        return builder.toString().toUpperCase(Locale.getDefault());
    }

    /**
     * 将int转为指定位数的字符串, 不足则高位补0
     */
    public static String int2String(int interger, int length) {
        String intStr = String.valueOf(interger);
        if (intStr.length() > length) {
            return intStr.substring(intStr.length() - length, intStr.length());
        } else if (intStr.length() == length) {
            return intStr;
        } else {
            StringBuilder builder = new StringBuilder(intStr);
            for (int i = 0; i < length - intStr.length(); i++) {
                builder.insert(0, '0');
            }
            return builder.toString();
        }
    }

    @SuppressLint({"DefaultLocale"})
    public static String bytesToHexString(byte[] bArray) {
        if (bArray == null) {
            return null;
        }
        if (bArray.length == 0) {
            return "";
        }
        StringBuffer sb = new StringBuffer(bArray.length);

        for (int i = 0; i < bArray.length; i++) {
            String sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    public static int byte2int2(byte[] b) {
        return b[1] & 0xFF | (b[0] & 0xFF) << 8;
    }

    public static byte[] concat(byte[]... bytesArray) {
        byte[] reulstBytes = new byte[0];
        for (int i = 0; i < bytesArray.length; i++) {
            reulstBytes = concat(reulstBytes, bytesArray[i]);
        }
        return reulstBytes;
    }

    public static byte[] concat(byte[] b1, byte[] b2) {
        if (b1 == null)
            return b2;
        if (b2 == null) {
            return b1;
        }
        byte[] result = new byte[b1.length + b2.length];
        System.arraycopy(b1, 0, result, 0, b1.length);
        System.arraycopy(b2, 0, result, b1.length, b2.length);
        return result;
    }

    public static int getXB(byte[] bdata2) {
        int i, j = 0, k = 0;
        for (i = 0; i < bdata2.length; i++) {
            if (bdata2[i] == 0x3D || bdata2[i] == 0x44) {
                j = i;
                k++;
                if (k == 1) {
                    return j;
                }
            }
        }
        return j;
    }

    public static byte[] byte2Bcd(byte[] abt) {
        if (abt == null) {
            return null;
        }
        int bbtLen = abt.length / 2;
        if (bbtLen == 0) {
            return new byte[0];
        }
        byte[] bbt = new byte[abt.length / 2];
        int j, k;
        for (int p = 0; p < abt.length / 2; p++) {
            if ((abt[2 * p] >= '0') && (abt[2 * p] <= '9')) {
                j = abt[2 * p] - '0';
            } else if ((abt[2 * p] >= 'a') && (abt[2 * p] <= 'z')) {
                j = abt[2 * p] - 'a' + 0x0a;
            } else {
                j = abt[2 * p] - 'A' + 0x0a;
            }

            if ((abt[2 * p + 1] >= '0') && (abt[2 * p + 1] <= '9')) {
                k = abt[2 * p + 1] - '0';
            } else if ((abt[2 * p + 1] >= 'a') && (abt[2 * p + 1] <= 'z')) {
                k = abt[2 * p + 1] - 'a' + 0x0a;
            } else {
                k = abt[2 * p + 1] - 'A' + 0x0a;
            }

            int a = (j << 4) + k;
            byte b = (byte) a;
            bbt[p] = b;
        }
        return bbt;
    }

    @SuppressLint({"DefaultLocale"})
    public String byteArrayToString(byte[] data) {
        if (data == null) {
            return null;
        }
        String str = "";
        String tempStr = "";
        if (data.length == 0) {
            return "";
        }
        for (int i = 0; i < data.length; i++) {
            tempStr = byteToString(data[i]);
            if (tempStr.length() == 1)
                tempStr = "0" + tempStr;
            str = str + tempStr;
        }
        return str.toUpperCase();
    }

    @SuppressLint({"DefaultLocale"})
    private String byteToString(byte buf) {
        int n = buf >= 0 ? buf : 256 + buf;
        String str = Integer.toHexString(n);
        return str.toUpperCase();
    }

    @SuppressLint({"DefaultLocale"})
    public char[] getCharByMethod(char[] a, int i, int j) {
        if (a == null) {
            return null;
        }
        int N = a.length;
        if (N == 0) {
            return new char[0];
        }
        StringBuffer s = new StringBuffer(" ");
        if ((i >= N) || (j >= N) || (i > j)) {
            return s.toString().toCharArray();
        }
        for (int x = i; x < j; x++) {
            s.append(a[x]);
        }
        return s.toString().trim().toCharArray();
    }

    @SuppressLint({"DefaultLocale"})
    public byte[] StringToBytes(String data) {
        String hexString = data.toUpperCase().trim();
        if (hexString.length() % 2 != 0) {
            return null;
        }
        byte[] retData = new byte[hexString.length() / 2];
        for (int i = 0; i < hexString.length(); i++) {
            int int_ch; // 锟斤拷位16锟斤拷锟斤拷锟斤拷转锟斤拷锟斤拷锟�10锟斤拷锟斤拷锟斤拷
            char hex_char1 = hexString.charAt(i); ////锟斤拷位16锟斤拷锟斤拷锟斤拷锟叫的碉拷一位(锟斤拷位*16)
            int int_ch1;
            if (hex_char1 >= '0' && hex_char1 <= '9')
                int_ch1 = (hex_char1 - 48) * 16; //// 0 锟斤拷Ascll - 48
            else if (hex_char1 >= 'A' && hex_char1 <= 'F')
                int_ch1 = (hex_char1 - 55) * 16; //// A 锟斤拷Ascll - 65
            else
                return null;
            i++;
            char hex_char2 = hexString.charAt(i); ///锟斤拷位16锟斤拷锟斤拷锟斤拷锟叫的第讹拷位(锟斤拷位)
            int int_ch2;
            if (hex_char2 >= '0' && hex_char2 <= '9')
                int_ch2 = (hex_char2 - 48); //// 0 锟斤拷Ascll - 48
            else if (hex_char2 >= 'A' && hex_char2 <= 'F')
                int_ch2 = hex_char2 - 55; //// A 锟斤拷Ascll - 65
            else
                return null;
            int_ch = int_ch1 + int_ch2;
            retData[i / 2] = (byte) int_ch;//锟斤拷转锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷Byte锟斤拷
        }
        return retData;
    }

    public static byte getNumHigh(int num) {
        return (byte) ((num & 0xFF00) >> 8);
    }

    public static byte getNumLow(int num) {
        return (byte) (num & 0xFF);
    }

    /**
     * 该行数在传入字符串不是偶数的时候会在字符串第0位插入一个"0", 再进行后面的操作
     *
     * @param asc
     * @return
     */
    public static byte[] hexString2Bytes(String asc) {
        int len = asc.length();
        int mod = len % 2;
        if (mod != 0) {
            asc = "0" + asc;
            len = asc.length();
        }
        byte[] abt = new byte[len];
        if (len >= 2) {
            len /= 2;
        }
        byte[] bbt = new byte[len];
        abt = asc.getBytes();

        for (int p = 0; p < asc.length() / 2; p++) {
            int j;
            if ((abt[(2 * p)] >= 48) && (abt[(2 * p)] <= 57)) {
                j = abt[(2 * p)] - 48;
            } else {
                if ((abt[(2 * p)] >= 97) && (abt[(2 * p)] <= 122))
                    j = abt[(2 * p)] - 97 + 10;
                else
                    j = abt[(2 * p)] - 65 + 10;
            }
            int k;
            if ((abt[(2 * p + 1)] >= 48) && (abt[(2 * p + 1)] <= 57)) {
                k = abt[(2 * p + 1)] - 48;
            } else {
                if ((abt[(2 * p + 1)] >= 97) && (abt[(2 * p + 1)] <= 122))
                    k = abt[(2 * p + 1)] - 97 + 10;
                else
                    k = abt[(2 * p + 1)] - 65 + 10;
            }
            int a = (j << 4) + k;
            byte b = (byte) a;
            bbt[p] = b;
        }
        return bbt;
    }


    public static void rwData(Context context, String readname, String writename) {
        try {
            InputStream is = context.getAssets().open(readname);
            FileOutputStream os = new FileOutputStream(writename);
            byte[] data = new byte[512];
            while ((is.read(data)) > 0) {
                os.write(data);
            }
            os.flush();
            os.close();
            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //解压

    public static void upZipFile(File zipFile, String folderPath) throws ZipException,
        IOException {
        File desDir = new File(folderPath);
        if (!desDir.exists()) {
            desDir.mkdirs();
        }
        ZipFile zf = new ZipFile(zipFile);
        for (Enumeration<?> entries = zf.entries(); entries.hasMoreElements(); ) {
            ZipEntry entry = ((ZipEntry) entries.nextElement());
            InputStream in = zf.getInputStream(entry);
            String str = folderPath + File.separator + entry.getName();
            str = new String(str.getBytes("8859_1"), "GB2312");
            File desFile = new File(str);
            if (!desFile.exists()) {
                File fileParentDir = desFile.getParentFile();
                if (!fileParentDir.exists()) {
                    fileParentDir.mkdirs();
                }
                desFile.createNewFile();
            }
            OutputStream out = new FileOutputStream(desFile);
            byte buffer[] = new byte[BUFF_SIZE];
            int realLength;
            while ((realLength = in.read(buffer)) > 0) {
                out.write(buffer, 0, realLength);
            }
            in.close();
            out.close();
            try {
                zf.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 解压assets目录下的zip到指定的路径
     *
     * @param zipFileString ZIP的名称，压缩包的名称：xxx.zip
     * @param outPathString 要解压缩路径
     * @throws Exception
     */
    public static void UnZipAssetsFolder(Context context, String zipFileString, String
        outPathString) throws Exception {
        ZipInputStream inZip = new ZipInputStream(context.getAssets().open(zipFileString));
        ZipEntry zipEntry;
        String szName = "";
        while ((zipEntry = inZip.getNextEntry()) != null) {
            szName = zipEntry.getName();
            if (zipEntry.isDirectory()) {
                //获取部件的文件夹名
                szName = szName.substring(0, szName.length() - 1);
                File folder = new File(outPathString + File.separator + szName);
                folder.mkdirs();
            } else {
                Log.e("UnZipAssets", outPathString + File.separator + szName);
                File file = new File(outPathString + File.separator + szName);
                if (!file.exists()) {
                    Log.e("UnZipAssets", "Create the file:" + outPathString + File.separator + szName);
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }
                // 获取文件的输出流
                FileOutputStream out = new FileOutputStream(file);
                int len;
                byte[] buffer = new byte[1024];
                // 读取（字节）字节到缓冲区
                while ((len = inZip.read(buffer)) != -1) {
                    // 从缓冲区（0）位置写入（字节）字节
                    out.write(buffer, 0, len);
                    out.flush();
                }
                out.close();
            }
        }
        inZip.close();
    }

    /**
     * GZip解压
     */
    public static boolean unGzipFile(String sourceFilePath) {
        Log.e("调试", "开始");
        String outputfilePath = "";
        try {
            File sourceFile = new File(sourceFilePath);
            if (!sourceFile.exists()) { // 如果目标文件不存在
                Log.e("error", "*.gz not exist");
                return false;
            }
            outputfilePath = sourceFilePath.substring(0, sourceFilePath.lastIndexOf('.'));
            outputfilePath += ".bmp";
            File outputfile = new File(outputfilePath);
            if (outputfile.exists()) {
                if (!outputfile.delete()) {
                    Log.e("warning", "*.bmp delete fail, deletefilePath = " + outputfilePath);
                }
            }
            // 建立gzip压缩文件输入流
            FileInputStream fin = new FileInputStream(sourceFilePath);
            // 建立gzip解压工作流
            GZIPInputStream gzin = new GZIPInputStream(fin);
            // 建立解压文件输出流
            FileOutputStream fout = new FileOutputStream(outputfile);
            int num;
            byte[] buf = new byte[1024];
            while ((num = gzin.read(buf, 0, buf.length)) != -1) {
                fout.write(buf, 0, num);
            }
            gzin.close();
            fout.close();
            fin.close();
            Log.e("调试", "结束");
            return true;
        } catch (Exception ex) {
            Log.e("调试", "异常");
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * GZip压缩
     *
     * @return 返回压缩后文件路径
     * @throws IOException
     * @throws FileNotFoundException sourceFilePath指向的文件不存在
     */
    public static String gzipFile(String sourceFilePath) throws FileNotFoundException,
        IOException {
        byte[] buf = new byte[2048];
        BufferedInputStream in =
            new BufferedInputStream(new FileInputStream(sourceFilePath));
        String destFilePath = sourceFilePath + ".gz";
        try {
            BufferedOutputStream out =
                new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(
                    destFilePath)));
            try {
                int c;
                while ((c = in.read(buf)) != -1) {
                    out.write(buf, 0, c);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
        return destFilePath;
    }

    public static byte[] zipData(byte[] data) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try (GZIPOutputStream gzOS = new GZIPOutputStream(os)) {
            gzOS.write(data, 0, data.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return os.toByteArray();
    }

    public static byte[] unzipData(byte[] data) {
        ByteArrayInputStream is = new ByteArrayInputStream(data);
        byte[] result = null;
        int packLen = 10 * 1024;
        byte[] temp = new byte[packLen];

        int len;
        try (GZIPInputStream gzIS = new GZIPInputStream(is)) {
            while ((len = gzIS.read(temp, 0, packLen)) != -1) {
                result = concat(result, arrayCopy(temp, 0, len));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取单个文件的MD5值！
     *
     * @param filePath
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public static String getFileMD5(String filePath) throws IOException, NoSuchAlgorithmException {
        File file = new File(filePath);
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        digest = MessageDigest.getInstance("MD5");
        in = new FileInputStream(file);
        while ((len = in.read(buffer, 0, 1024)) != -1) {
            digest.update(buffer, 0, len);
        }
        in.close();

//		BigInteger bigInt = new BigInteger(1, digest.digest());
//		return bigInt.toString(16);
        return byte2HexStr(digest.digest());
    }

    public static boolean deleteFile(String deleteFilePath) {
        try {
            File file = new File(deleteFilePath);
            if (file.exists()) {
                file.delete();
            }
            return true;
        } catch (Exception e) {
            Log.e("warning", "删除指定文件时出现异常");
            e.printStackTrace();
            return false;
        }
    }

    // 复制文件
    public static void copyFile(File sourceFile, File targetFile) throws IOException {
        BufferedInputStream inBuff = null;
        BufferedOutputStream outBuff = null;
        try {
            inBuff = new BufferedInputStream(new FileInputStream(sourceFile));
            outBuff = new BufferedOutputStream(new FileOutputStream(targetFile));
            byte[] b = new byte[1024 * 5];
            int len;
            while ((len = inBuff.read(b)) != -1) {
                outBuff.write(b, 0, len);
            }
            outBuff.flush();
        } finally {
            if (inBuff != null)
                inBuff.close();
            if (outBuff != null)
                outBuff.close();
        }
    }

    /**
     * bytes字符串转换为Byte值
     *
     * @param hexString Byte字符串，每个Byte之间没有分隔符
     * @return byte[]
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase(Locale.US);
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] =
                (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    /**
     * 16进制字符数据转换为10进制字节数据
     */
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static byte[] charToByte2(char c) {
        byte[] b = new byte[2];
        b[0] = (byte) ((c & 0xFF00) >> 8);
        b[1] = (byte) (c & 0xFF);
        return b;
    }

    /**
     * 字符串转换成十六进制字符串
     *
     * @param str 待转换的ASCII字符串
     * @return String 每个Byte之间无空格分隔，如: [61 6C 6B]
     */
    public static String str2HexStr(String str) {
        if (str == null || str.equals("")) {
            return "";
        }
        byte[] bs = str.getBytes();
        return byte2HexStr(bs);
    }

    /**
     * 字符串转换成十六进制字符串
     *
     * @param str     待转换的ASCII字符串
     * @param charset 指定字符集
     * @return String 每个Byte之间无空格分隔，如: [61 6C 6B]
     */
    public static String str2HexStr(String str, String charset) {
        if (str == null || str.equals("")) {
            return "";
        }
        byte[] bs;
        try {
            bs = str.getBytes(charset);
            return byte2HexStr(bs);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("字符串转byte[]出错, 指定的字符集是: " + charset);
        }
    }

    /**
     * 16进制字符串转十进制数
     *
     * @param str
     * @return
     */
    public static int hexStr2Int(String str) {
        if (str == null || str.equals("")) {
            return 0;
        }
        return Integer.parseInt(str, 16);
    }

    public static Map<String, String> parseIcUserInfo2(byte[] data, String charsetName) {
        HashMap<String, String> map = new HashMap<String, String>();
        try {
            int point = 0;
            while (point < data.length - 4) {
                byte[] tagByte = new byte[1];
                byte[] lenByte = new byte[3];
                String tag, tagData;
                tagByte[0] = data[point];
                tag = new String(tagByte);
                point = point + 1;
                System.arraycopy(data, point, lenByte, 0, 3);
                point = point + 3;
                int len = Integer.valueOf(new String(lenByte));
                byte[] tagDataByte = new byte[len];
                System.arraycopy(data, point, tagDataByte, 0, len);
                point = point + len;
                tagData = new String(tagDataByte, charsetName);
                map.put(tag, tagData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public static byte[] int2bytes(int value, int size) {
        byte buf[] = new byte[size];
        for (int i = buf.length - 1; i >= 0; i--) {
            int bitCount = 8 * (buf.length - i - 1);
            if (bitCount < 32) {
                buf[i] = (byte) (value >>> bitCount);
            } else {
                buf[i] = 0;
            }
        }
        return buf;
    }

    /**
     * bytes转换成十六进制字符串
     *
     * @param b byte数组
     * @return String 每个Byte值之间无空格分隔
     */
    public static String byte2HexStr(byte[] b) {
        String stmp = "";
        StringBuilder sb = new StringBuilder("");
        if (b != null && b.length > 0) {
            for (int n = 0; n < b.length; n++) {
                stmp = Integer.toHexString(b[n] & 0xFF);
                sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
                sb.append("");
            }
        }
        return sb.toString().toUpperCase(Locale.US).trim();
    }

    /**
     * bytes转换成十六进制字符串
     *
     * @param b byte数组
     * @return String 每个Byte值之间无空格分隔
     */
    public static String byte2HexStr(byte b) {
        String stmp = "";
        StringBuilder sb = new StringBuilder("");
        stmp = Integer.toHexString(b & 0xFF);
        sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
        sb.append("");
        return sb.toString().toUpperCase(Locale.US).trim();
    }

    /**
     * bytes转换成十六进制字符串
     *
     * @param b byte数组
     * @return String 每个Byte值之间无空格分隔
     */
    public static String byte2HexStr(byte[] b, int offset, int length) {
        String stmp = "";
        StringBuilder sb = new StringBuilder("");
        if (b != null && b.length > 0) {
            for (int n = offset; n < length; n++) {
                stmp = Integer.toHexString(b[n] & 0xFF);
                sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
                sb.append("");
            }
        }
        return sb.toString().toUpperCase(Locale.US).trim();
    }


    /**
     * CHK 值计算
     * byte[]进行异或,byte返回；
     *
     * @param b
     * @return 16进制字符串
     */
    public static byte[] byteXOR(byte[] b) {
        byte[] r = new byte[1];
        if (b != null && b.length > 0) {
            for (int i = 0; i < b.length; i++) {
                if (i == 0)
                    r[0] = b[i];
                else
                    r[0] = (byte) (r[0] ^ b[i]);
            }
        }
        return r;
    }

    /**
     * 将  以分为单位的纯数字字符串  转换为  以元为单位的数字字符串
     *
     * @param numStrInUtilsOfCents 以分为单位的纯数字字符串
     * @return 返回以元为单位的数字字符串，该字符串有一个小数点，显示到0后两位，例如0.00
     */
    public static String formatFromCentsToYuan(String numStrInUtilsOfCents) {
        if (numStrInUtilsOfCents == null || numStrInUtilsOfCents.equals("")) {
            return "";
        }

        /*
         * 判断字符串内容是否为纯数字
         */
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(numStrInUtilsOfCents);
        if (!isNum.matches()) {
            throw new RuntimeException("纯数字字符串：\"" + numStrInUtilsOfCents + "\"，中出现非数字字符");
        }

        /*
         * 给位数不够的字符串开头补0
         */
        StringBuilder builder = new StringBuilder(numStrInUtilsOfCents);
        if (builder.length() < 3) {
            builder.insert(0, "00");
        }
        /*
         * 去除高位多余的0
         */
        builder.insert(builder.length() - 2, ".");
        while (builder.charAt(0) == '0' && builder.indexOf(".") != 1) {
            builder.deleteCharAt(0);
        }
        return builder.toString();
    }

    /**
     * 将位图对象bmp中所有不透明白色转换成透明色
     *
     * @param bmp 将要转换的位图对象
     * @return 转换好的位图对象
     */
    public static Bitmap toAlph(Bitmap bmp) {
        return toAlph(bmp, null);
    }

    /**
     * 将位图对象bmp中所有不透明白色转换成透明色
     *
     * @param bmp            将要转换的位图对象
     * @param outputFilePath 输出图片文件路径
     * @return 转换好的位图对象
     */
    public static Bitmap toAlph(Bitmap bmp, String outputFilePath) {

        int color = 0x00FFFFFF;//透明色
        int bitmap_w = bmp.getWidth();
        int bitmap_h = bmp.getHeight();
        int[] arrayColor = new int[bitmap_w * bitmap_h];
        int count = 0;
        for (int i = 0; i < bitmap_h; i++) {
            for (int j = 0; j < bitmap_w; j++) {
                int color1 = bmp.getPixel(j, i);

                //白色处理成透明色，其他颜色不变
                if (color1 == 0xffffffff) {
                    color1 = color;
                }
                arrayColor[count] = color1;
                count++;
            }
        }
        bmp = Bitmap.createBitmap(arrayColor, bitmap_w, bitmap_h, Config.ARGB_8888);
        if (outputFilePath != null) {
            FileOutputStream out = null;
            File outfile = new File(outputFilePath);
            if (bmp != null) {
                try {
                    out = new FileOutputStream(outfile);
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return bmp;
    }

    /**
     * 计算LRC校验值
     *
     * @param data
     * @param dataLength
     * @return
     */
    public static byte calcLRC(byte[] data, int dataLength) {

        // LogUtil.e("data的数据", Util.bytesToHexString(data));
        if (data == null || dataLength == 0) {
            return 0;
        }

        byte lrc = 0;
        for (int i = 0; i < dataLength; i++) {
            lrc ^= data[i];
            // LogUtil.e("lrc", i+"--------"+lrc);
        }

        return lrc;
    }

    public static byte calcLRC(byte[] data, int offset, int len) {
        if (data == null || len == 0) {
            return 0;
        }
        byte lrc = 0;
        len += offset;
        for (int i = offset; i < len; i++) {
            lrc ^= data[i];
        }
        return lrc;
    }

    public static byte[] arrayCopy(byte[] src, int len) {
        return arrayCopy(src, 0, len);
    }

    /**
     * 获取子数组
     *
     * @param src       源数组
     * @param srcOffset 源数组上的偏移
     * @param len       截取长度
     * @return 返回截取的子数组
     */
    public static byte[] arrayCopy(byte[] src, int srcOffset, int len) {
        byte[] dst = new byte[len];
        System.arraycopy(src, srcOffset, dst, 0, len);
        return dst;
    }

    public static byte[] subBytes(byte[] src, int start, int end) {
        return arrayCopy(src, start, end - start);
    }


    /**
     * Unicode转中文方法
     **/
    public static String unicodeToCn(String unicode) {
        /** 以 \ u 分割，因为java注释也能识别unicode，因此中间加了一个空格*/
        String[] strs = unicode.split("\\\\u");
        String returnStr = "";
        // 由于unicode字符串以 \ u 开头，因此分割出的第一个字符是""。
        for (int i = 1; i < strs.length; i++) {
            returnStr += (char) Integer.valueOf(strs[i], 16).intValue();
        }
        return returnStr;
    }

    /**
     * 中文转Unicode
     **/
    public static String cnToUnicode(String cn) {
        char[] chars = cn.toCharArray();
        StringBuilder returnStr = new StringBuilder();
        for (char aChar : chars) {
            String uni = Integer.toString(aChar, 16);
            if (uni.length() < 4) {
                uni ="00" + uni ;
            }
            returnStr.append("\\u").append(uni);
        }
        return returnStr.toString();
    }

    /**
     * byteBuffer 转 byte数组
     *
     * @param buffer
     * @return
     */
    public static byte[] bytebuffer2ByteArray(ByteBuffer buffer) {
        //重置 limit 和postion 值
        buffer.flip();
        //获取buffer中有效大小
        int len = buffer.limit() - buffer.position();

        byte[] bytes = new byte[len];

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = buffer.get();

        }

        return bytes;
    }

    public static String string2AsciiString(String srcString) {
        StringBuffer sbu = new StringBuffer();
        char[] chars = srcString.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            sbu.append((int)chars[i]);
        }
        return sbu.toString();
    }
    @NonNull
    public static <T> T checkNotNull(@Nullable T obj) {
        if (obj == null) {
            throw new NullPointerException();
        } else {
            return obj;
        }
    }
}
