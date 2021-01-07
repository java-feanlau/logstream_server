package com.boyitech.logstream.core.util;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.boyitech.logstream.core.setting.CacheSettings;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Continent;
import com.maxmind.geoip2.record.Country;
import com.maxmind.geoip2.record.Location;
import com.maxmind.geoip2.record.Postal;
import com.maxmind.geoip2.record.Subdivision;

public class GeoIPHelper {

	private final static String PATH = Paths.get(FilePathHelper.ROOTPATH, "geoip", "GeoLite2-City.mmdb").toString();
	static final Logger LOGGER = LogManager.getLogger("main");
	private File database;
	private DatabaseReader reader = null;
	private static Map<String, Map> geoIpMap = new HashMap<String, Map>();
	private int cacheSize = 200000;

    private static class GeoIPHelperHolder {
        private static final GeoIPHelper INSTANCE = new GeoIPHelper();
    }

    private GeoIPHelper (){
    	database = new File(PATH);
    	try {
    		cacheSize = CacheSettings.GEOIPCACHESIZE.getValue();
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    	try {
			reader = new DatabaseReader.Builder(database).build();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    public static final GeoIPHelper getInstance() {
        return GeoIPHelperHolder.INSTANCE;
    }

	/**
	 * 查找ip对应的地理信息，如果geoip库中没有该ip，则返回null
	 * @param ip
	 * @return HashMap or null
	 * @throws IOException
	 * @throws GeoIp2Exception
	 */
	public Map getGeoIPInfo(String ip) throws IOException, GeoIp2Exception {
		Map c_ip_geoip;
		if(geoIpMap.get(ip)!=null) {
			return geoIpMap.get(ip);
		}else {
			c_ip_geoip = new HashMap();
			InetAddress ipAddress = InetAddress.getByAddress(IPv4Util.ipToBytesByInet(ip));
			CityResponse response;
			try {
				response = reader.city(ipAddress);
			}catch(AddressNotFoundException e) {
				return null;
			}
			Country country = response.getCountry();
			City city = response.getCity();
			Location location = response.getLocation();
		    Continent continent = response.getContinent();
		    Postal postal = response.getPostal();
		    Subdivision subdivision = response.getMostSpecificSubdivision();
			c_ip_geoip.put("city_name", city.getName());
			c_ip_geoip.put("timezone", location.getTimeZone());
			c_ip_geoip.put("ip", ip);
			c_ip_geoip.put("latitude", location.getLatitude());
			c_ip_geoip.put("country_code2", country.getIsoCode());
			c_ip_geoip.put("country_name", country.getName());
			c_ip_geoip.put("continent_code", continent.getCode());
			c_ip_geoip.put("country_code3", country.getIsoCode());
			c_ip_geoip.put("region_name", subdivision.getName());
	        Double latitude = location.getLatitude();
	        Double longitude = location.getLongitude();
	        if (latitude != null && longitude != null) {
	//          Map<String, Object> locationObject = new HashMap<>();
	//          locationObject.put("lat", latitude);
	//          locationObject.put("lon", longitude);
	//          c_ip_geoip.put("location", locationObject);
	        	Double[] d = {longitude, latitude};
	        	c_ip_geoip.put("location", d);
	        }
			c_ip_geoip.put("longitude", location.getLongitude());
			c_ip_geoip.put("region_code", subdivision.getIsoCode());
			if(geoIpMap.size() < cacheSize) {
				geoIpMap.put(ip, c_ip_geoip);
			}
		}
		return c_ip_geoip;
	}

	public void close() {
		try {
			if(reader!=null)
				reader.close();
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}
	}

	public static int getCacheSize() {
		return geoIpMap.size();
	}

}
