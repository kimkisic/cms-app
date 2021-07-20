//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.choistec.cms.scannerReg.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class CommonUtil {
    public CommonUtil() {
    }

    public static String nvl(String value) {
        try {
            return value != null && value.length() != 0 ? value : "";
        } catch (Exception var2) {
            return "";
        }
    }

    /**
     * 한글 영문 숫자 점 만 추출
     */
    public static String extractWord(String szVictim) {

        //int iCodeSp = 32;                         // Space
        int iCodeSp = 46;                         // 점
        int iCodeNum[] = {48, 57};          // 숫자 0 ~ 9
        int iCodeEngLow[] = {65, 90};          // 영문 A ~ Z
        //int iCodeEngUpp[] = {97, 122};         // 영문 a ~ z
        /**
         * 모든 아스키코드 데이터 범위를 선언해본다.
         */
        int iCodeACSIIUpp[] = {32, 127};         // 영문 a ~ z
        int iCodeKorChar[] = {44032, 55203};    // 한글 가 ~ 힣

        //String szVictim = "「문자열」에서 '한글', '영문', '숫자', Space 만 남겨두고 (특수문자 ★ 같은 것들도 당연히) 모두 지운다.";
        String szReplaceTo = "";
        String szResult = new String("");

        int iCharCode = 0;
        int iIdx = 0;

        boolean bCheck = true;

        System.out.println("String Length : " + szVictim.length());

        szResult = "";
        for (iIdx = 0; iIdx < szVictim.length(); iIdx++) {

            iCharCode = (int) szVictim.charAt(iIdx);

            bCheck = true;

            if (iCharCode != iCodeSp) {
                if (iCharCode < iCodeNum[0] || iCharCode > iCodeNum[1]) {
                    if (iCharCode < iCodeEngLow[0] || iCharCode > iCodeEngLow[1]) {
                        if (iCharCode < iCodeACSIIUpp[0] || iCharCode > iCodeACSIIUpp[1]) {
                            if (iCharCode < iCodeKorChar[0] || iCharCode > iCodeKorChar[1]) {
                                szResult += szReplaceTo;
                                bCheck = false;
                            }
                        }
                    }
                }
            }

            if (bCheck) {
                szResult += (char) iCharCode;
            }

        }

        System.out.println("Original : " + szVictim);
        System.out.println("Result   : " + szResult);
        return szResult;
    }

    /**
     * 소프트웨어의 현재 버전을 가져온다.
     */
    public static String getVersion(Context context) {
        String version = "";
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

}
