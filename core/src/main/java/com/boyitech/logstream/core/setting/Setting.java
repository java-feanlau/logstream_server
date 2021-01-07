package com.boyitech.logstream.core.setting;

import java.util.Map;
import java.util.function.Function;

/**
 * @Author Eric Zheng
 * @Description 调用getValue 如果配置文件中没有则读取程序设置的配置。
 * @Date 17:10 2019/3/7
 * @Param
 * @return
 **/
public class Setting<T> extends BaseSettings{

	private String key;
	private String value;
	private Function<String, T> parser;
	private Function<String, String> seeker;

	private Setting() {
		// 私有构造方法，使得Setting无法通过new来实例化
		// Setting只能通过提供的静态公有方法来获取实例
	}

	private Setting(String key, String value, Function<String, T> parser) {
		this.key = key;
		this.value = value;
		this.parser = parser;
		this.seeker = null;
	}


	public T getValue() {
		String value = null;
		//判断是否存在从settings中取值的方法对象
		//有则调用，没有则用默认的getValueInSettings
		if (seeker != null) {
			value = seeker.apply(key).toString();
		} else {
			value = getValueInSettings(this.key);
		}
		//调用解析的方法解析获取的值并返回
		if (value == null) {
			return parser.apply(this.value);
		} else {
			return parser.apply(value);
		}
	}

	/**
	 * 从传入的config中查找key对应的�?�，如果没有从settings中寻找�?�，如果没有使用自身的默认�??
	 * @param key
	 * @param config
	 * @return value
	 */
	public T getValue(String key, Map config) {
		String value = null;
		Object obj = config.get(key);
		if(obj!=null) {
			value = obj.toString();
		} else {
			//判断是否存在从settings中取值的方法对象
			//有则调用，没有则用默认的getValueInSettings
			if (seeker != null) {
				value = seeker.apply(key).toString();
			} else {
				value = getValueInSettings(this.key);
			}
		}
		//调用解析的方法解析获取的值并返回
		if (value == null) {
			return parser.apply(this.value);
		} else {
			return parser.apply(value);
		}
	}

	public static Setting<Boolean> booleanSetting(String key, boolean value) {
		return new Setting<>(key, Boolean.toString(value), (String s) -> parseBoolean(key, s));
	}

	public static Setting<String> stringSetting(String key, String value) {
		return new Setting<>(key, value, (String s) -> parseString(key, s));
	}

	public static Setting<Integer> integerSetting(String key, int value) {
		return new Setting<>(key, Integer.toString(value), (String s) -> parseInteger(key, s));
	}

	public static Setting<Double> doubleSetting(String key, double value) {
		return new Setting<>(key, Double.toString(value), (String s) -> parseDouble(key, s));
	}

	public static Setting<Long> longSetting(String key, long value) {
		return new Setting<>(key, Long.toString(value), (String s) -> parseLong(key, s));
	}

	public static Setting<Float> floatSetting(String key, float value) {
		return new Setting<>(key, Float.toString(value), (String s) -> parseFloat(key, s));
	}

	private static Boolean parseBoolean(String key, String value) {
		return Boolean.parseBoolean(value);
	}

	private static String parseString(String key, String value) {
		return value;
	}

	private static Integer parseInteger(String key, String value) {
		return ((Number)Float.parseFloat(value)).intValue();
	}

	private static Double parseDouble(String key, String value) {
		return Double.parseDouble(value);
	}

	private static Long parseLong(String key, String value) {
		return Long.parseLong(value);
	}

	private static Float parseFloat(String key, String value) {
		return Float.parseFloat(value);
	}

	private static String getValueInSettings(String key) {
		return Settings.get(key);
	}



}
