package james.com.simplezhihudaily.Model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 登录的Cookies
 * 以单件模式维护
 */
public class Cookies implements Serializable{
    @SerializedName("q_c1")
    private String q_c1;
    @SerializedName("a_t")
    private String a_t;
    @SerializedName("z_c0")
    private String z_c0;
    @SerializedName("cap_id")
    private String cap_id;
    @SerializedName("l_cap_id")
    private String l_cap_id;
    @SerializedName("login")
    private String login;
    @SerializedName("n_c")
    private String n_c;
    @SerializedName("_xsrf")
    private String xsrf;
    private static Cookies cookies;

    public synchronized static Cookies getInstance(){
        return cookies;
    }

    private Cookies(String q_c1, String a_t, String z_c0, String cap_id, String l_cap_id, String login, String n_c, String xsrf) {
        this.q_c1 = q_c1;
        this.a_t = a_t;
        this.z_c0 = z_c0;
        this.cap_id = cap_id;
        this.l_cap_id = l_cap_id;
        this.login = login;
        this.n_c = n_c;
        this.xsrf = xsrf;
    }

    public String getQ_c1() {
        return q_c1;
    }

    public void setQ_c1(String q_c1) {
        this.q_c1 = q_c1;
    }

    public String getA_t() {
        return a_t;
    }

    public void setA_t(String a_t) {
        this.a_t = a_t;
    }

    public String getZ_c0() {
        return z_c0;
    }

    public void setZ_c0(String z_c0) {
        this.z_c0 = z_c0;
    }

    public String getCap_id() {
        return cap_id;
    }

    public void setCap_id(String cap_id) {
        this.cap_id = cap_id;
    }

    public String getL_cap_id() {
        return l_cap_id;
    }

    public void setL_cap_id(String l_cap_id) {
        this.l_cap_id = l_cap_id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getN_c() {
        return n_c;
    }

    public void setN_c(String n_c) {
        this.n_c = n_c;
    }

    public String getXsrf() {
        return xsrf;
    }

    public void setXsrf(String xsrf) {
        this.xsrf = xsrf;
    }
}
