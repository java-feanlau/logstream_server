package com.boyitech.logstream.server.test;

import com.boyitech.logstream.core.util.jdbc.DBUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author Eric
 * @Title: JdbcTest
 * @date 2019/2/19 9:42
 * @Description: TODO
 */
public class JdbcTest {
    public static void main(String args[]) throws SQLException {
//        String json = "{" +
//                "\"moduleType\": \"file\"," +
//                "\"index\": \"example_apache_access_111\"," +
//                "\"readPath\": [\"C:/Users/Eric/Desktop/testShipper\"]," +
//                //"\"fileNameMatch\": \"1*.txt\"," +
//                "\"threadPollMax\": \"1\"," +
//                "\"ignoreOld\": \"false\"," +
//                "\"ignoreFileOfTime\": \"86400\"," +
//                "\"saveOffsetTime\": \"5\"," +
//                "\"encoding\": \"utf8\"," +
//                "\"secondOfRead\": \"5\"" +
//                "}";
//        Map<String, Object> map = new HashMap<>();
//        map.put("shipper_id", "1");
//        map.put("shipper_config", json);
//        map.put("lv1cache", "1");
//        try {
////            int count = DBUtil.insert("ys_worker_shipper", map);
//            HashMap<String, Object> map1 = new HashMap<>();
//            map1.put("shipper_id", "1");
//            DBUtil.delete("ys_worker_shipper",map1);
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }


//        try {
//            List<Map<String, Object>> list = DBUtil.query("select * from ys_cache");
//            for (Map<String, Object> stringObjectMap : list) {
//                System.out.println(stringObjectMap);
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
        try {
            List<Map<String, Object>> cacheList = DBUtil.query("select * from ys_cache");
            for (Map<String, Object> stringObjectMap : cacheList) {
                System.out.println("1");
            }

        } catch (SQLException e) {

        }
    }
}
