package cn.bob.twitter;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.pipeline.JsonFilePipeline;

import java.io.*;
import java.net.URL;

import net.sourceforge.tess4j.*;

public class ProxyPool  implements PageProcessor {
    private Site site = Site.me().setRetryTimes(3).setSleepTime(100);

    @Override
    public void process(Page page) {
                page.addTargetRequests(page.getHtml().links().regex("(https://proxy\\.mimvp\\.com/freeopen\\.php\\?proxy=out_hp&sort=p_checkdtime&page=\\d+)").all());
        page.putField("ip",page.getHtml().xpath("//td[@class='tbl-proxy-ip']/text()").toString());
        page.putField("port",code("https://proxy.mimvp.com/"+page.getHtml().xpath("//td[@class='tbl-proxy-port']/img/@src").toString(),"runtime"+System.currentTimeMillis()+".png"));
//        page.putField("port",code("https://img2018.cnblogs.com/blog/1196212/201901/1196212-20190115180038005-332025874.png","runtime"+System.currentTimeMillis()+".png"));
        page.putField("type",page.getHtml().xpath("//td[@class='tbl-proxy-type']/text()").toString());
        System.out.print(page.getResultItems());
    }

    public String code(String file,String time){
        try {
            File imageFile = downloadImage(file, time);
            String code = recognizeText(imageFile,"png");
            System.out.println("结果是："+code);
            return code;
        } catch (TesseractException e) {
            System.out.print(e.getMessage());
        } catch (IOException e) {
            System.out.print(e.getMessage());
        } catch (Exception ee) {
            System.out.println(ee.getMessage());
        }
        return "no";
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static synchronized String recognizeText(File imageFile, String imageFormat) throws Exception {
        File outputFile = new File(imageFile.getParentFile(), "output");
        StringBuffer strB = new StringBuffer();
        String outfile = "/home/sunweibo/桌面/Site/twitter/picture/output";
        String[] cm=new String[]{"tesseract",imageFile.getAbsolutePath(),outfile};
        System.out.println("执行的命令是    ");
        for(String str:cm){
            System.out.print(str+" ");
        }
        Process pb = Runtime.getRuntime().exec(cm);
        int w = pb.waitFor();
        if (w == 0) {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(outputFile
                    .getAbsolutePath()
                    + ".txt"), "UTF-8"));
            System.out.println("正在读取"+outputFile
                    .getAbsolutePath()
                    + ".txt 文件");
            String str;
            while ((str = in.readLine()) != null) {
                strB.append(str);
            }
            System.out.println("读取完成 结果是 "+strB.toString());
            in.close();
        } else {
            String msg;
            switch (w) {
                case 1:
                    msg = "Errors accessing files. There may be spaces in your image's filename.";
                    break;
                case 29:
                    msg = "Cannot recognize the image or its selected region.";
                    break;
                case 31:
                    msg = "Unsupported image format.";
                    break;
                default:
                    msg = "Errors occurred.";
            }
//	           tempImage.delete();
            throw new RuntimeException(msg);
        }
//	       new File(outputFile.getAbsolutePath() + ".txt").delete();
        return strB.toString();
    }


    public static File downloadImage(String Imageurl,String filename) throws IOException {
        System.getProperties().setProperty("http.proxyHost", "IP");//设置代理
        System.getProperties().setProperty("http.proxyPort", "Port");
        URL url = new URL(Imageurl);
        //打开网络输入流
        DataInputStream dis = new DataInputStream(url.openStream());
        String newImageName = "/home/sunweibo/桌面/Site/twitter/picture/" + filename;
        //建立一个新的文件
        FileOutputStream fos = new FileOutputStream(new File(newImageName));
        byte[] buffer = new byte[1024];
        int length;
        //开始填充数据
        while ((length = dis.read(buffer)) > 0) {
            fos.write(buffer, 0, length);
        }
        dis.close();
        fos.close();
        return new File(newImageName);
    }

    public static void main(String[] args) {
        ProxyPool pool = new ProxyPool();
        Spider.create(pool)
                .addUrl("https://proxy.mimvp.com/freeopen.php?proxy=out_hp&sort=p_checkdtime&page=1")
                .addPipeline(new JsonFilePipeline("/home/sunweibo/桌面/Site/twitter/proxydata"))
                .run();
    }
}