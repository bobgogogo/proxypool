package cn.bob.twitter;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import net.sourceforge.tess4j.*;

import javax.imageio.ImageIO;

public class ProxyPool  implements PageProcessor {
    private Site site = Site.me().setRetryTimes(3).setSleepTime(100);

    @Override
    public void process(Page page) {
//        page.addTargetRequests(page.getHtml().links().regex("(https://proxy\\.mimvp\\.com/freeopen\\.php\\?proxy=out_hp&sort=p_checkdtime&page=\\d+)").all());
        page.putField("ip",page.getHtml().xpath("//td[@class='tbl-proxy-ip']/text()").toString());
        page.putField("port",code("https://proxy.mimvp.com/"+page.getHtml().xpath("//td[@class='tbl-proxy-port']/img/@src").toString()));
        page.putField("type",page.getHtml().xpath("//td[@class='tbl-proxy-type']/text()").toString());
        System.out.print(page.getResultItems());
    }

    public String code(String file) {
        File imageFile = new File(file);

        try {
            BufferedImage image = ImageIO.read(imageFile);
            ImageIO.write(image, "jpg",new File("tessdata/aaa"));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        ITesseract instance = new Tesseract();
        instance.setDatapath("tessdata"); // path to tessdata directory

        try {
            File image = new File("tessdata/aaa");
            ImageIO.scanForPlugins();
            String result = instance.doOCR(image);
            System.out.println(result);
            return result;
        } catch (TesseractException e) {
            System.err.println(e.getMessage());
        }
        String str = "no";
        return str;
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        ProxyPool pool = new ProxyPool();
        Spider.create(pool)
                .addUrl("https://proxy.mimvp.com/freeopen.php?proxy=out_hp&sort=p_checkdtime&page=1")
                .run();
    }
}