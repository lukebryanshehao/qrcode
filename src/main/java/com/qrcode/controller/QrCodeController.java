package com.qrcode.controller;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.qrcode.model.ResultData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Calendar;
import java.util.Hashtable;


@RestController
@RequestMapping("/app/v1")
public class QrCodeController {

    @Value("${other.filesavepath}")
    private String FileSavePath;

    @RequestMapping(value="/hello", method = RequestMethod.GET)
    @ResponseBody
    public String getString() {
        return "hello";
    }

    //解析二维码
    @RequestMapping(value = "/decode", method = RequestMethod.POST)
    public String decode(@RequestParam("fileName") MultipartFile file) {
        if (file.isEmpty()) {
            return new ResultData().NewResultData("10001", "", "file isEmpty");
        }
        String fileName = file.getOriginalFilename();

        File dest = new File(FileSavePath + "/qrcode/" + fileName);
        if (!dest.getParentFile().exists()) { //判断文件父目录是否存在
            dest.getParentFile().mkdirs();
        }
        try {
            file.transferTo(dest); //保存文件
            //解析二维码
            return new ResultData().NewResultData("200", readQrCode(new FileInputStream(dest)), "Success");
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return new ResultData().NewResultData("10001", "", e.toString());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return new ResultData().NewResultData("10001", "", e.toString());
        }
    }

    //生成二维码
    @RequestMapping(value = "/encode", method = RequestMethod.POST)
    public String encode(@RequestParam("content") String content, @RequestParam("qrcodesize") int qrcodesize) {
        System.out.println("Create QRCode Content: "+content);
        System.out.println("Create QRCode QRCodeSize: "+qrcodesize);

        try {

            String fileName = Calendar.getInstance().getTimeInMillis() + "";

            File file = new File(FileSavePath+"/createCode/"+fileName+".jpg");

            if (!file.getParentFile().exists()) { //判断文件父目录是否存在
                file.getParentFile().mkdir();
            }

            boolean flag = createQrCode(new FileOutputStream(file), content, qrcodesize, "jpg");

            if (flag) {
                FileInputStream inputFile = new FileInputStream(file);
                byte[] buffer = new byte[(int) file.length()];
                inputFile.read(buffer);
                inputFile.close();
                return new ResultData().NewResultData("200", new BASE64Encoder().encode(buffer), "Success");
            }
            return new ResultData().NewResultData("10001", "", "Fail");
        } catch (WriterException e) {
            e.printStackTrace();
            return new ResultData().NewResultData("10001", "", e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            return new ResultData().NewResultData("10001", "", e.toString());
        }
    }

    /**
     * 读二维码并输出携带的信息
     */
    public static String readQrCode(InputStream inputStream) throws IOException {
        //从输入流中获取字符串信息
        BufferedImage image = ImageIO.read(inputStream);
        //将图像转换为二进制位图源
        LuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        Result result = null;
        try {
            result = reader.decode(bitmap);
        } catch (ReaderException e) {
            e.printStackTrace();
        }
        return result.getText();
    }

    /**
     * 生成包含字符串信息的二维码图片
     *
     * @param outputStream 文件输出流路径
     * @param content      二维码携带信息
     * @param qrCodeSize   二维码图片大小
     * @param imageFormat  二维码的格式
     * @throws WriterException
     * @throws IOException
     */
    public static boolean createQrCode(OutputStream outputStream, String content, int qrCodeSize, String imageFormat) throws WriterException, IOException {
        //设置二维码纠错级别ＭＡＰ
        Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<EncodeHintType, ErrorCorrectionLevel>();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);  // 矫错级别
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        //创建比特矩阵(位矩阵)的QR码编码的字符串
        BitMatrix byteMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize, hintMap);
        // 使BufferedImage勾画QRCode  (matrixWidth 是行二维码像素点)
        int matrixWidth = byteMatrix.getWidth();
        BufferedImage image = new BufferedImage(matrixWidth - 200, matrixWidth - 200, BufferedImage.TYPE_INT_RGB);
        image.createGraphics();
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, matrixWidth, matrixWidth);
        // 使用比特矩阵画并保存图像
        graphics.setColor(Color.BLACK);
        for (int i = 0; i < matrixWidth; i++) {
            for (int j = 0; j < matrixWidth; j++) {
                if (byteMatrix.get(i, j)) {
                    graphics.fillRect(i - 100, j - 100, 1, 1);
                }
            }
        }
        return ImageIO.write(image, imageFormat, outputStream);
    }

//    /**
//     * 测试代码
//     *
//     * @throws WriterException
//     */
//    public static void main(String[] args) throws IOException, WriterException {
//
//        createQrCode(new FileOutputStream(new File("d:\\qrcode.jpg")), "www.baidu.com", 900, "JPEG");
//        readQrCode(new FileInputStream(new File("D:\\develop\\workspace\\goworkspace\\src\\wechatmanagent\\static\\qrcode\\2019\\0522\\21558497220043462100.jpg")));
//    }
}
