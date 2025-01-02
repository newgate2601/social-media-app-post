package com.example.social_media_app_post.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class StringUtils {
    public static String convertListToString(List<String> imageUrls){
        System.out.println();
        if (Objects.isNull(imageUrls) || imageUrls.isEmpty()){
            return null;
        }
        StringBuilder result = new StringBuilder();
        Integer index = 0;
        for (String imageUrl : imageUrls){   // anh1;anh2;anh3
            result.append(imageUrl);
            if (index != imageUrls.size() - 1){
                result.append(";");
            }
            index ++;
        }
        return result.toString();
    }

    public static List<String> getListFromString(String imageUrls){
        if (Objects.isNull(imageUrls)) {
            return new ArrayList<>();
        }
        return Arrays.asList(imageUrls.split(";"));
    }
}
