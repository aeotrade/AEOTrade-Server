package com.aeotrade.provider.file.upload.property;

public enum FileTypeEnum {
    SSO,LOCAL;

    public static boolean isEnum(String value){
        switch (value){
            case "SSO":
            case "LOCAL":
                return true;
            default:
                return false;
        }
    }

}
