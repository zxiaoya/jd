package com.jd.core;

import com.alibaba.fastjson.JSONObject;
import com.jd.utils.HttpUtilRequest;
import com.jd.model.Pager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.cookie.Cookie;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

/**
 * Created by zxy with 2018/5/10.
 *
 * @author QAQ
 */
public class JdAuto {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(JdAuto.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        String pid = "4079304";
        login(pid);
    }

    private static void login(String pid) throws IOException, InterruptedException {
        //首次访问首页
        Pager loginpage = HttpUtilRequest.get("https://passport.jd.com/new/login.aspx");
        //通过访问首页获取到访问Cookie
        List<Cookie> cookies = loginpage.getCookies();

        Map<String, String> headers = HttpUtilRequest.commonHeaders();
        headers.put("Cookie", HttpUtilRequest.createHeaderCookie(cookies));
        headers.put("Host", "qr.m.jd.com");
        headers.put("Referer", "https://passport.jd.com/new/login.aspx");
        Pager qrpage = HttpUtilRequest.get(headers, "https://qr.m.jd.com/show?appid=133&size=147&t=1525929580517", HttpUtilRequest.GET_STREAM);
        byte[] bytes = qrpage.getBytes();
        List<Cookie> qrcookies = qrpage.getCookies();

        headers.put("Cookie", HttpUtilRequest.createHeaderCookie(qrcookies));
        String token = null;
        for (Cookie cookie : qrcookies)
        {
            if ("wlfstk_smdl".equals(cookie.getName()))
            {
                token = cookie.getValue();
            }
        }
        FileUtils.writeByteArrayToFile(new File("D://QR.png"), bytes);

        if (StringUtils.isEmpty(token))
        {
            System.out.println("请求异常稍后请重试");
        }

        //最多等待60S
        int time = 60;

        while (time-- > 0)
        {
            //jQuery3610289({
            //   "code" : 201,
            //   "msg" : "二维码未扫描 ，请扫描二维码"
            //})
            Pager qrmessage = HttpUtilRequest.get(headers, "https://qr.m.jd.com/check?callback=jQuery4442005&appid=133&token=" + token + "&_=" + System.currentTimeMillis());


            String html = qrmessage.getHtml();

            String qrCode = getQrCode(html);
            String qrMsg = getQrMsg(html);

            //还未扫码
            if (StringUtils.isNotEmpty(qrCode))
            {
                if ("201".equals(qrCode.trim()))
                {
                    System.out.println("qrMsg = " + qrMsg);
                }
                //请在手机确认
                if ("202".equals(qrCode))
                {
                    System.out.println("qrMsg = " + qrMsg);
                }
                //扫码成功
                if ("200".equals(qrCode))
                {
                    String ticket = getQrTicket(html);
                    String cookie = headers.get("Cookie");
                    headers = HttpUtilRequest.commonAjaxHeaders();
                    headers.put("Cookie", cookie);
                    headers.put("Host", "passport.jd.com");
                    headers.put("Referer", "https://passport.jd.com/uc/login");
                    Pager qrCodeTicketValidation = HttpUtilRequest.get(headers, "https://passport.jd.com/uc/qrCodeTicketValidation?t=" + ticket);
                    HttpUtilRequest.addCookieFromSetCookieHeader(cookies, qrCodeTicketValidation.getHeaders());
                    List<Cookie> sucessCookies = qrCodeTicketValidation.getCookies();


                    String curCookie = HttpUtilRequest.createHeaderCookie(sucessCookies);
                    headers.put("Cookie", curCookie);
                    headers.put("Host", "cart.jd.com");
                    headers.put("Referer", "https://item.jd.com/" + pid + ".html");

                    LOGGER.info(String.format("正在查找商品编号 %s 的商品", pid));
                    Pager addCart302 = HttpUtilRequest.get(headers, "https://cart.jd.com/gate.action?pid=" + pid + "&pcount=1&ptype=1");
                    int statusCode = addCart302.getStatusCode();
                    if (statusCode == 302)
                    {
                        LOGGER.debug("正在帮您将商品添加到购物车");
                        String location = addCart302.getLocation();

                        String rcd = getRcd(location);

                        Connection connect = Jsoup.connect(location);
                        connect.cookies(headers);
                        Document document = connect.get();
                        if (document.toString().contains("商品已成功加入购物车") && "1".equals(rcd))
                        {
                            sleep(2);
                            LOGGER.debug("商品已成功加入购物车");
                        }
                        else
                        {
                            LOGGER.debug("商品添加失败");
                            JOptionPane.showMessageDialog(null, "商品添加失败", "出错啦", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        Pager orderListPage = HttpUtilRequest.get(headers, "https://cart.jd.com/cart.action?r=" + Math.random());
                        System.out.println("orderListPage = " + orderListPage.getHtml().contains("游戏办机械键盘"));

                        HttpUtilRequest.addCookieFromSetCookieHeader(cookies, orderListPage.getHeaders());

                        curCookie = HttpUtilRequest.createHeaderCookie(cookies);
                        headers.put("Cookie", curCookie);
                        LOGGER.info("正在准备为您创建订单");
                        Pager orderinfopage302 = HttpUtilRequest.get(headers, "https://cart.jd.com/gotoOrder.action?rd=" + Math.random());

                        int statusCode1 = orderinfopage302.getStatusCode();
                        System.out.println("statusCode1 = " + statusCode1);

                        if (statusCode1 == 302)
                        {
                            try
                            {
                                String orderInfoUrl = orderinfopage302.getLocation();

                                headers.put("Host", "trade.jd.com");
                                headers.put("Referer", "https://cart.jd.com/cart.action?r=0.42079717275348693");
                                Pager httpUtilNewModel = HttpUtilRequest.get(headers, orderInfoUrl);
                                html = httpUtilNewModel.getHtml();
                                Document document2 = Jsoup.parse(html);

                                String riskControl = document2.getElementById("riskControl").attr("value");

                                if (StringUtils.isNotEmpty(riskControl))
                                {
                                    LOGGER.debug("订单页面加载完毕,准备下单");
                                }
                                else
                                {
                                    LOGGER.info("订单页面记载失败,稍后在尝试");
                                    JOptionPane.showMessageDialog(null, "订单页面记载失败,稍后在尝试", "alert", JOptionPane.ERROR_MESSAGE);
                                    return;
                                }

                                String taskId = "";
                                String ab = "";
                                for (Cookie cc : cookies)
                                {
                                    String name = cc.getName();

                                    if ("TrackID".equals(name))
                                    {
                                        taskId = cc.getValue();
                                    }
                                    if ("3AB9D23F7A4B3C9B".equals(name))
                                    {
                                        ab = cc.getValue();
                                    }
                                }
                                String param = "overseaPurchaseCookies=&submitOrderParam.sopNotPutInvoice=false&submitOrderParam.trackID=" + taskId + "&submitOrderParam.ignorePriceChange=0&submitOrderParam.btSupport=0&submitOrderParam.eid=" + ab + "&submitOrderParam.fp=caeadd9c1a0322a845206cf3e42f6360&riskControl=" + riskControl;

                                BasicNameValuePair[] carnvps = new BasicNameValuePair[]{new BasicNameValuePair("param", param)};

                                Map<String, String> headers1 = HttpUtilRequest.commonAjaxHeaders();
                                curCookie = HttpUtilRequest.createHeaderCookie(cookies);
                                headers1.put("Cookie", curCookie);
                                Pager session = HttpUtilRequest.post(headers1, null, carnvps, "https://trade.jd.com/shopping/order/submitOrder.action", HttpUtilRequest.REQ_PARAMS_ENTITY);

                                JSONObject jsonObject = JSONObject.parseObject(session.getHtml());

                                String success = jsonObject.getString("success");

                                if ("true".equals(success))
                                {
                                    LOGGER.info("下单成功,请去手机APP或京东官方网站支付");
                                }
                                else
                                {
                                    LOGGER.info("下单失败");
                                    JOptionPane.showMessageDialog(null, "下单失败", "alert", JOptionPane.ERROR_MESSAGE);
                                }

                            } catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                        else
                        {
                            LOGGER.info("下单失败");
                            JOptionPane.showMessageDialog(null, "下单失败", "alert", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    else
                    {
                        LOGGER.info("添加购物车失败 请检查商品ID是否正确或稍后在尝试");
                        JOptionPane.showMessageDialog(null, "添加购物车失败 请检查商品ID是否正确或稍后在尝试", "alert", JOptionPane.ERROR_MESSAGE);
                    }

                    break;
                }
                //二维码超时
                if ("203".equals(qrCode))
                {
                    System.out.println("qrMsg = " + qrMsg);
                    return;
                }
            }
            sleep(1);
        }


    }

    private static String getQrCode(String html) {
        Pattern compile = compile("\"code\" : (.*?),");
        Matcher matcher = compile.matcher(html);
        if (matcher.find())
        {
            return matcher.group(1);
        }
        return null;
    }

    private static String getQrMsg(String html) {
        Pattern compile = compile("\"msg\" : \"(.*?)\"");
        Matcher matcher = compile.matcher(html);
        if (matcher.find())
        {
            return matcher.group(1);
        }
        return null;
    }

    private static String getQrTicket(String html) {
        Pattern compile = compile("\"ticket\" : \"(.*?)\"");
        Matcher matcher = compile.matcher(html);
        if (matcher.find())
        {
            return matcher.group(1);
        }
        return null;
    }

    private static void sleep(int a) throws InterruptedException {
        Thread.sleep((long) 1000 * a);
    }

    private static String getRcd(String str) {
        Pattern compile = compile("rcd=(.*?)&");
        Matcher matcher = compile.matcher(str);

        if (matcher.find())
        {
            String group = matcher.group(1);
            System.out.println(String.format("获取到的RCD = %s", group));
            return group;
        }
        return null;
    }

}
