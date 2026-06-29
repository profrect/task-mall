package com.mall.common.core.context;

import lombok.Builder;

import java.util.Collection;
import java.util.Objects;

public class UserContext {

    private static final ThreadLocal<User> USER_INFO_LOCAL = new ThreadLocal<>();

    public static User current() {
        return USER_INFO_LOCAL.get();
    }

    public static void setUser(User user){
        USER_INFO_LOCAL.set(user);
    }

    public static String currentUserId() {
        User user = USER_INFO_LOCAL.get();
        if (Objects.isNull(user)) {
            return null;
        }
        return user.userId();
    }

    public static String currentUsername() {
        User user = USER_INFO_LOCAL.get();
        if (Objects.isNull(user)) {
            return null;
        }
        return user.username();
    }

    public static void clear(){
        USER_INFO_LOCAL.remove();
    }

    @Builder
    public record User(
            String userId,
            String username,
            boolean superPermission,
            Collection<String> roles,
            Collection<String> roleMenus,
            Collection<String> roleApis
    ){}
}
