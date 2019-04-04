import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class GUETLogin {
    public static String LoginUrl = "http://10.32.254.11/a70.htm";
    public static String logoutUrl = "http://10.32.254.11/F.htm";
    public static String formUrl = "http://10.32.254.11:801/eportal/extern/huajiang_2018041101/ip/3/pc.js";
    public static String UserAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.86 Safari/537.36";
    public static String Connection = "keep-alive";
    private static String userName = "";
    private static String passwd = "";
    public int key = 0;

    /**
     * 分离连接方法，方便调用
     *
     * @param Url
     * @return
     */
    public static org.jsoup.Connection connection(String Url) {
        org.jsoup.Connection jconnection = Jsoup.connect(Url);
        jconnection.header("UserAgent", UserAgent);
        jconnection.followRedirects(true);
        jconnection.header("Connection", Connection);
        return jconnection;
    }

    /**
     * 登录方法
     *
     * @param userName
     * @param passwd
     * @throws Exception
     */
    public void login(String userName, String passwd) throws Exception {
        key = 1;
        this.userName = userName;
        this.passwd = passwd;
        Map<String, String> form = getForm();
        System.out.println("打印表单信息：\n" + form);
        org.jsoup.Connection jconnection = connection(LoginUrl);
        Response rs = jconnection.method(Method.GET).execute();
        Response login = jconnection.method(Method.POST).data(form).cookies(rs.cookies()).execute();
        int rscode = login.statusCode();
        System.out.println("状态码" + rscode);//200是正常连接，203是连接被拒绝
        Map<String, String> coockies = rs.cookies();
        System.out.println("cookies:" + coockies);//校园网不用cookie，不过在其它情景如教务系统cookie是必要的
//        System.out.println(login.body());
        outputPage(login);
	checkConn();
    }

    /**
     * 注销方法
     */
    public void logout() throws IOException {
        Response rs = connection(logoutUrl).execute();
        System.out.println("状态码" + rs.statusCode());
        System.out.println("已登出");

    }

    /**
     * 检查连接
     */

    public void checkConn() {
        String baiduUrl = "https://www.baidu.com";
        Response rs = null;
        try {
            connection(baiduUrl).execute();
            System.out.println("连接正常");
            if (key == 1) System.out.println("登陆成功了");
        } catch (Exception e) {
            if (key == 0) System.out.println("连接到百度服务器失败，请登录");
            else if (key == 1) System.out.println("连接到百度服务器失败，请检查你的账号密码是否正确");
        }
        try {
            System.out.println("状态码" + rs.statusCode());
        } catch (NullPointerException e) {

        }
    }

    /**
     * 这个方法是为了获取pc.js文件，因为Jsoup不能直接解析js文件，只能通过输入流来获取这个文件
     * 创建一个流对象接收这个文件，先转为String对象，再转化为Document对象，之后才能解析
     */
    public Map<String, String> getForm() throws Exception {
        URLConnection url = new URL(formUrl).openConnection();
        url.setRequestProperty("UserAgent", UserAgent);
        url.setRequestProperty("Connection", Connection);
        url.setReadTimeout(10000);
        InputStream is = url.getInputStream();
        byte[] by = new byte[10240];
        int len = is.read(by);
        by = Arrays.copyOf(by, len);
        String formText = new String(by);
        is.close();

        Document document = Jsoup.parse(formText);//将获得的含有form的字符串转为Document，如此才能解析里面的表单
        List<Element> elementList = document.select("form");
        Map<String, String> form = new HashMap<>();
        for (int i = 0; i < elementList.size(); i++)
            for (Element element : elementList.get(i).getAllElements()) {
                if (element.attr("name").equals("DDDDD")) {
                    element.attr("value", userName);
                }
                if (element.attr("name").equals("upass")) {
                    element.attr("value", passwd);
                }
                if (element.attr("name").equals("R2")) {
                    element.attr("value", "");
                }
                form.put(element.attr("name"), element.attr("value"));
            }
        return form;


    }


    /**
     * 将返回的网页保存到本地
     */
    public void outputPage(Response rs) throws IOException {
        File page = new File("./log/", "returnPage.txt");
        if (!page.exists()) new File("./log").mkdir();
        FileOutputStream fos = new FileOutputStream(page);
        fos.write(rs.bodyAsBytes());
        fos.close();
    }

    /**
     * 保存cookie到本地
     *
     * @param rs
     */
    public void outputCookie(Response rs) {
        File cookie = new File("./log/cookie.txt");

        FileOutputStream fos;
        try {
            fos = new FileOutputStream(cookie);
            fos.write(rs.cookies().toString().getBytes());
            fos.close();
        } catch (FileNotFoundException f) {
            System.out.println("该文件不存在");

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("输出异常，请确认是否拥有写权限，如必要请使用chmod 755 *");
        } finally {
        }
    }

    GUETLogin() {
        checkConn();
    }

}

class Login {
    public static void main(String[] args) throws Exception {
        GUETLogin login = new GUETLogin();
        Scanner scanner = new Scanner(System.in);
        System.out.print("ID:");
        String user = scanner.next();
        System.out.print("Password:");
        String passwd = scanner.next();
        login.login(user, passwd);
    }
}

class Logout {
    public static void main(String[] args) throws IOException {
        new GUETLogin().logout();
    }
}
