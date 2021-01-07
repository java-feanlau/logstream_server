package com.boyitech.logstream.core.util;

import com.boyitech.logstream.core.info.Event;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * @author juzheng
 * @Title: MetafieldHelper
 * @date 2019/8/16 4:55 PM
 * @Description: 完善Metafield字段的辅助类
 */
public class MetafieldHelper {

    /*
    * @Author juzheng
    * @Description 对metafield字段3个ip进行存储
    * @Date 2:56 PM 2019/8/19
    * @Param [event, Metafield_source, Metafield_object, Metafield_subject, format]
    * @return void
    */
    public static void setMetafield(Event event, String Metafield_source, String Metafield_object, String Metafield_subject, Map format){
        String metafield_source = String.valueOf(format.get(Metafield_source));
        String metafield_object = String.valueOf(format.get(Metafield_object));
        String metafield_subject = String.valueOf(format.get(Metafield_subject));

        if(event.getSource()!=null){
            event.setSource(event.getSource());
        }
        if(GrokUtil.isStringHasValue(metafield_source)&&Metafield_source!=""){
            event.setMetafieldSource(metafield_source);
        }
        if(GrokUtil.isStringHasValue(metafield_object)&&Metafield_object!=""){
           event.setMetafieldObject(metafield_object);
        }
        if(GrokUtil.isStringHasValue(metafield_subject)&&Metafield_subject!=""){
            event.setMetafieldSubject(metafield_subject);
        }

        if(!GrokUtil.isStringHasValue(metafield_object)&&GrokUtil.isStringHasValue(metafield_subject)){
            event.setMetafieldObject(metafield_subject);
        }

//        if(!GrokUtil.isStringHasValue(metafield_subject)&&GrokUtil.isStringHasValue(metafield_object)){
//            event.setMetafieldSubject(metafield_object);
//        }

    }

    public static void setMetafieldLoglevel(Event event,String level){
        event.setMetafieldLoglevel(level);
    }
}
