package ink.markidea.note.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 *@author  hansanshi
 */
@Slf4j
public class JsonUtil {

    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        initObjMapper();
    }

    /**
     * init objectMapper
     */
    private static void initObjMapper(){
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS,false);
    }

    /**
     * Convert an object into a string
     * @param obj
     * @param <T>
     * @return
     */
    public static <T> String objToString(T obj){

        if (obj == null){
            return null;
        }

        if (obj instanceof String ){
            return (String)obj;
        }

        try {
            return objectMapper.writeValueAsString(obj);
        } catch (IOException e) {
            log.error("parse object to string error",e);
            return null;
        }
    }

    /**
     * Convert an object into a prettified string
     * @param obj
     * @param <T>
     * @return
     */
    public static <T> String objToStringPretty(T obj){

        if (obj == null){
            return null;
        }

        if (obj instanceof String ){
            return (String)obj;
        }

        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (IOException e) {
            log.error("parse object to string error",e);
            return null;
        }
    }

    /**
     * Convert a string into a object
     */
    public static <T> T stringToObj(String string,Class<T> clazz){
        if (StringUtils.isEmpty(string) || clazz == null){
            return null;
        }

        if (clazz.equals(String.class)){
            return (T)string;
        }

        try {
            return objectMapper.readValue(string,clazz);
        } catch (IOException e) {
            log.error("Parse string to object ",e);
            return null;
        }
    }

    /**
     * Convert a string into a object
     * example : List<User> newUserList = JsonUtil.stringToObj(listToStr, new TypeReference<List<User>>() {});
     */
    public static <T> T stringToObj(String string, TypeReference<T> typeReference){
        if (StringUtils.isEmpty(string) || typeReference == null){
            return null;
        }

        if (typeReference.getType().equals(String.class)){
            return (T)string;
        }

        try {
            return objectMapper.readValue(string,typeReference);
        } catch (IOException e) {
            log.error("Parse string to object ",e);
            return null;
        }
    }

    /**
     * Convert a string into a object
     */
    public static <T> T stringToObj(String string,Class<?> collectionClass,Class<?>... classes){
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(collectionClass,classes);

        if (StringUtils.isEmpty(string)  ){
            return null;
        }

        try {
            return objectMapper.readValue(string,javaType);
        } catch (IOException e) {
            log.error("Parse string to object ",e);
            return null;
        }
    }

}
