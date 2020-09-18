package com.manyox.msprinter.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.Hashtable;

/**
 * <pre>
 *     author: szf
 *     time  : 2018/08/03
 *     desc  : 二维码生成工具类
 * </pre>
 */
public class QrCodeUtil {

    /**
     * 创建二维码位图
     *
     * @param content 字符串内容
     * @param size    位图宽&高(单位:px)
     * @return
     */
    @Nullable
    public static Bitmap createQRCodeBitmap(@Nullable String content, int size) {
        return createQRCodeBitmap(content, size, "UTF-8", "H", "4", Color.BLACK, Color.WHITE, null, null, 0F);
    }

    /**
     * 创建二维码位图 (自定义黑、白色块颜色)
     *
     * @param content     字符串内容
     * @param size        位图宽&高(单位:px)
     * @param color_black 黑色色块的自定义颜色值
     * @param color_white 白色色块的自定义颜色值
     * @return
     */
    @Nullable
    public static Bitmap createQRCodeBitmap(@Nullable String content, int size, @ColorInt int color_black, @ColorInt int color_white) {
        return createQRCodeBitmap(content, size, "UTF-8", "H", "4", color_black, color_white, null, null, 0F);
    }

    /**
     * 创建二维码位图 (带Logo小图片)
     *
     * @param content     字符串内容
     * @param size        位图宽&高(单位:px)
     * @param logoBitmap  logo图片
     * @param logoPercent logo小图片在二维码图片中的占比大小,范围[0F,1F]。超出范围->默认使用0.2F
     * @return
     */
    @Nullable
    public static Bitmap createQRCodeBitmap(String content, int size, @Nullable Bitmap logoBitmap, float logoPercent) {
        return createQRCodeBitmap(content, size, "UTF-8", "H", "4", Color.BLACK, Color.WHITE, null, logoBitmap, logoPercent);
    }

    /**
     * 创建二维码位图 (Bitmap颜色代替黑色) 注意!!!注意!!!注意!!! 选用的Bitmap图片一定不能有白色色块,否则会识别不出来!!!
     *
     * @param content      字符串内容
     * @param size         位图宽&高(单位:px)
     * @param targetBitmap 目标图片 (如果targetBitmap != null, 黑色色块将会被该图片像素色值替代)
     * @return
     */
    @Nullable
    public static Bitmap createQRCodeBitmap(String content, int size, Bitmap targetBitmap) {
        return createQRCodeBitmap(content, size, "UTF-8", "H", "4", Color.BLACK, Color.WHITE, targetBitmap, null, 0F);
    }

    /**
     * 创建二维码位图 (支持自定义配置和自定义样式)
     * @param content          字符串内容
     * @param size             位图宽&高(单位:px)
     * @param character_set    字符集/字符转码格式 (支持格式:{@link CharacterSetECI })。传null时,zxing源码默认使用 "ISO-8859-1"
     * @param error_correction 容错级别 (支持级别:{@link ErrorCorrectionLevel })。传null时,zxing源码默认使用 "L"
     * @param margin           空白边距 (可修改,要求:整型且>=0), 传null时,zxing源码默认使用"4"。
     * @param color_black      黑色色块的自定义颜色值
     * @param color_white      白色色块的自定义颜色值
     * @param targetBitmap     目标图片 (如果targetBitmap != null, 黑色色块将会被该图片像素色值替代)
     * @param logoBitmap       logo小图片
     * @param logoPercent      logo小图片在二维码图片中的占比大小,范围[0F,1F],超出范围->默认使用0.2F。
     * @return
     */
    @Nullable
    public static Bitmap createQRCodeBitmap(@Nullable String content, int size,
                                            @Nullable String character_set, @Nullable String error_correction, @Nullable String margin,
                                            @ColorInt int color_black, @ColorInt int color_white, @Nullable Bitmap targetBitmap,
                                            @Nullable Bitmap logoBitmap, float logoPercent) {

        //1.参数合法性判断
        if (TextUtils.isEmpty(content)) { // 字符串内容判空
            return null;
        }

        if (size <= 0) {  // 宽&高都需要>0
            return null;
        }

        try {
            //2.设置二维码相关配置,生成BitMatrix(位矩阵)对象
            Hashtable<EncodeHintType, String> hints = new Hashtable<>();

            if (!TextUtils.isEmpty(character_set)) {
                hints.put(EncodeHintType.CHARACTER_SET, character_set); // 字符转码格式设置
            }

            if (!TextUtils.isEmpty(error_correction)) {
                hints.put(EncodeHintType.ERROR_CORRECTION, error_correction); // 容错级别设置
            }

            if (!TextUtils.isEmpty(margin)) {
                hints.put(EncodeHintType.MARGIN, margin); // 空白边距设置
            }
            BitMatrix bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints);

            //3.根据BitMatrix(位矩阵)对象为数组元素赋颜色值
            if (targetBitmap != null) {
                targetBitmap = Bitmap.createScaledBitmap(targetBitmap, size, size, false);
            }
            int[] pixels = new int[size * size];
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    if (bitMatrix.get(x, y)) { // 黑色色块像素设置
                        if (targetBitmap != null) {
                            pixels[y * size + x] = targetBitmap.getPixel(x, y);
                        } else {
                            pixels[y * size + x] = color_black;
                        }
                    } else { // 白色色块像素设置
                        pixels[y * size + x] = color_white;
                    }
                }
            }

            //4.创建Bitmap对象,根据像素数组设置Bitmap每个像素点的颜色值,之后返回Bitmap对象
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, size, 0, 0, size, size);

            //5.为二维码添加logo小图标
            if (logoBitmap != null) {
                return addLogo(bitmap, logoBitmap, logoPercent);
            }

            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 向一张图片中间添加logo小图片(图片合成)
     * @param srcBitmap   原图片
     * @param logoBitmap  logo图片
     * @param logoPercent 百分比 (用于调整logo图片在原图片中的显示大小, 取值范围[0,1], 传值不合法时使用0.2F)
     *                    原图片是二维码时,建议使用0.2F,百分比过大可能导致二维码扫描失败。
     * @return
     */
    @Nullable
    private static Bitmap addLogo(@Nullable Bitmap srcBitmap, @Nullable Bitmap logoBitmap, float logoPercent) {

        //1. 参数合法性判断
        if (srcBitmap == null) {
            return null;
        }

        if (logoBitmap == null) {
            return srcBitmap;
        }

        if (logoPercent < 0F || logoPercent > 1F) {
            logoPercent = 0.2F;
        }

        // 2. 获取原图片和Logo图片各自的宽、高值
        int srcWidth = srcBitmap.getWidth();
        int srcHeight = srcBitmap.getHeight();
        int logoWidth = logoBitmap.getWidth();
        int logoHeight = logoBitmap.getHeight();

        //3. 计算画布缩放的宽高比
        float scaleWidth = srcWidth * logoPercent / logoWidth;
        float scaleHeight = srcHeight * logoPercent / logoHeight;

        //4. 使用Canvas绘制,合成图片
        Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(srcBitmap, 0, 0, null);
        canvas.scale(scaleWidth, scaleHeight, srcWidth / 2, srcHeight / 2);
        canvas.drawBitmap(logoBitmap, srcWidth / 2 - logoWidth / 2, srcHeight / 2 - logoHeight / 2, null);

        return bitmap;
    }


    /**
     * 删除白边
     * */
    private static BitMatrix deleteWhite(BitMatrix matrix) {
        int[] rec = matrix.getEnclosingRectangle();
        int resWidth = rec[2] ;
        int resHeight = rec[3] ;
        BitMatrix resMatrix = new BitMatrix(resWidth, resHeight);
        resMatrix.clear();
        for (int i = 0; i < resWidth; i++) {
            for (int j = 0; j < resHeight; j++) {
                if (matrix.get(i + rec[0], j + rec[1]))
                    resMatrix.set(i, j);
            }
        }
        return resMatrix;
    }

    //获取二维码Bitmap
    public static Bitmap getQRcode(String content, int width, int height){
        Hashtable<EncodeHintType, Object> hashTable = new Hashtable<EncodeHintType, Object>();
        hashTable.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hashTable.put(EncodeHintType.CHARACTER_SET, "utf-8");
        hashTable.put(EncodeHintType.MARGIN, 0);
        Bitmap bitmap =null;
        try {
            BitMatrix bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hashTable);
            bitMatrix = deleteWhite(bitMatrix);
            width = bitMatrix.getWidth();
            height = bitMatrix.getHeight();
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * width + x] = 0xff000000;
                    }
                    else{
                        pixels[y * width + x] = 0xffffffff;
                    }
                }
            }
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            //createCenterBitmap2(bitMatrix, bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 获取单色位图
     * @param inputBMP
     * @return Bitmap
     */
    public static Bitmap getSinglePic(Bitmap inputBMP) {
        int[] pix = new int[inputBMP.getWidth() * inputBMP.getHeight()];
        int[] colorTemp = new int[inputBMP.getWidth() * inputBMP.getHeight()];
        inputBMP.getPixels(pix, 0, inputBMP.getWidth(), 0, 0,
                inputBMP.getWidth(), inputBMP.getHeight());
        Bitmap returnBMP = Bitmap.createBitmap(inputBMP.getWidth(),
                inputBMP.getHeight(), Bitmap.Config.ARGB_8888);
        int lightNumber = 127;// 曝光度，這個顔色是中間值，如果大於中間值，那就是黑色，否則白色，数值越小，曝光度越高
        for (int j = 0; j < colorTemp.length; j++) {
            if(pix[j] == 0){
                colorTemp[j] = -1;
            }
            else{
                colorTemp[j] = Color.rgb(Color.red(pix[j]), Color.green(pix[j]),
                        Color.blue(pix[j]));
            }
        }
        for (int i = 0; i < colorTemp.length; i++) {
            // 這裏需要思考一下，上一步有可能得到：純紅，純黃，純藍，黑色，白色這樣5種顔色，前三種是應該變成白色還是黑色呢？
            // 發現這是一個很複雜的問題，涉及到不同區域閒顔色的對比，如果是黑色包圍紅色，那紅色就應該是白色，反之變成黑色。。。
            // 似乎衹能具體問題具體分析，這裏就先把黃色設成白色，藍色=白色，紅色=黑色
            int r = Color.red(pix[i]);
            int g = Color.green(pix[i]);
            int b = Color.blue(pix[i]);
            // 有兩種顔色以上的混合，那就是變成黑色但目前这种方法，对于黑白的曝光效果更出色，
            // 原理是设置一个曝光值，然后三种颜色相加大于3倍的曝光值，才是黑色，否则白色
            //两个二维码拼接处pix[i]是0，应该是白色，单独处理  cl
            if(pix[i] == 0){
                colorTemp[i] = Color.rgb(255, 255, 255);
            }
            else if (r + g + b > 3 * lightNumber) {
                colorTemp[i] = Color.rgb(255, 255, 255);
            } else {
                colorTemp[i] = Color.rgb(0, 0, 0);
            }
        }
        returnBMP.setPixels(colorTemp, 0, inputBMP.getWidth(), 0, 0,
                inputBMP.getWidth(), inputBMP.getHeight());
        return returnBMP;
    }

    /**
     * 把两个位图覆盖合成为一个位图，左右拼接
     * @param leftBitmap
     * @param rightBitmap
     * @param isBaseMax 是否以宽度大的位图为准，true则小图等比拉伸，false则大图等比压缩
     * @return
     */
    public static Bitmap mergeBitmap_LR(Bitmap leftBitmap, Bitmap rightBitmap, boolean isBaseMax) {

        if (leftBitmap == null || leftBitmap.isRecycled()
                || rightBitmap == null || rightBitmap.isRecycled()) {
            //JDLog.logError(TAG, "leftBitmap=" + leftBitmap + ";rightBitmap=" + rightBitmap);
            return null;
        }
        int height = 0; // 拼接后的高度，按照参数取大或取小
        if (isBaseMax) {
            height = leftBitmap.getHeight() > rightBitmap.getHeight() ? leftBitmap.getHeight() : rightBitmap.getHeight();
        } else {
            height = leftBitmap.getHeight() < rightBitmap.getHeight() ? leftBitmap.getHeight() : rightBitmap.getHeight();
        }

        // 缩放之后的bitmap
        Bitmap tempBitmapL = leftBitmap;
        Bitmap tempBitmapR = rightBitmap;

        if (leftBitmap.getHeight() != height) {
            tempBitmapL = Bitmap.createScaledBitmap(leftBitmap, (int)(leftBitmap.getWidth()*1f/leftBitmap.getHeight()*height), height, false);
        } else if (rightBitmap.getHeight() != height) {
            tempBitmapR = Bitmap.createScaledBitmap(rightBitmap, (int)(rightBitmap.getWidth()*1f/rightBitmap.getHeight()*height), height, false);
        }

        // 拼接后的宽度
        int width = tempBitmapL.getWidth() + tempBitmapR.getWidth() + 10;

        // 定义输出的bitmap
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // 缩放后两个bitmap需要绘制的参数
        Rect leftRect = new Rect(0, 0, tempBitmapL.getWidth(), tempBitmapL.getHeight());
        Rect rightRect  = new Rect(0, 0, tempBitmapR.getWidth(), tempBitmapR.getHeight());

        // 右边图需要绘制的位置，往右边偏移左边图的宽度，高度是相同的
        Rect rightRectT  = new Rect(tempBitmapL.getWidth() + 10, 0, width, height);

        canvas.drawBitmap(tempBitmapL, leftRect, leftRect, null);
        canvas.drawBitmap(tempBitmapR, rightRect, rightRectT, null);
        return bitmap;
    }

    /**
     * jpg png bmp 彩色图片转换Bitmap数据为int[]数组
     * @param bm
     * @return int[]
     */
    public static int[] getPixelsByBitmap(Bitmap bm) {
        int width, heigh;
        width = bm.getWidth();
        heigh = bm.getHeight();
        int iDataLen = width * heigh;
        int[] pixels = new int[iDataLen];
        bm.getPixels(pixels, 0, width, 0, 0, width, heigh);
        return pixels;
    }
}
