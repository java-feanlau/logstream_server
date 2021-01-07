package com.boyitech.logstream.core.util.enums;

/**
 * @author juzheng
 * @Title: MonthEnum
 * @date 2019/7/22 11:27 AM
 * @Description: 月份（月份简写+中文）的枚举类
 */
public enum  MonthEnum {
    Jan("一月"),
    Feb("二月"),
    Mar("三月"),
    Apr("四月"),
    May("五月"),
    Jun("六月"),
    Jul("七月"),
    Aug("八月"),
    Sep("九月"),
    Oct("十月"),
    Nov("十一月"),
    Dec("十二月");

    private String chineseMonth;

    private static final MonthEnum[] ENUMS = MonthEnum.values();

    private MonthEnum(String chineseMonth) {
        this.chineseMonth=chineseMonth;
    }

    public static MonthEnum of(String chineseMonth) {
        for(MonthEnum m : MonthEnum.values()) {
            if(m.chineseMonth.equals(chineseMonth)) {
                return m;
            }
        }
        return null;
    }

    public String getValue() {
        return this.chineseMonth;
    }

}
