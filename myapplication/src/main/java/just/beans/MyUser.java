package just.beans;

import cn.bmob.v3.BmobUser;

public class MyUser extends BmobUser {
    private String name;

    public MyUser(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}