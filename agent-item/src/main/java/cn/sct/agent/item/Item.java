package cn.sct.agent.item;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author bigdata_dpy
 */
public interface Item<T> {


   default Class<T> getValueClass(){
       Type genericSuperclass = getClass().getGenericSuperclass();
       if (genericSuperclass instanceof ParameterizedType) {
           ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
           Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
           if (actualTypeArguments.length > 0) {
               Type actualType = actualTypeArguments[0];
               if (actualType instanceof Class<?>) {
                   return (Class<T>) actualType;
               }
           }
       }
       return (Class<T>) Object.class;
   } ;
    /**
     * 默认是类名
     *
     * @return 通用网管物模型的id
     */
    default String name() {
        return this.getClass().getSimpleName();
    }

    /**
     * @return 读取属性逻辑
     */
    T getValue();

    /**
     * @param value 设置的值,调用set 有返回值，则认为set进入设备，并被设备应用
     * @return 若成功返回设置的值
     */
    default T setValue(T value) {
        return value;
    }



    /**
     * 主动上报逻辑
     */
    default T report() {
        return null;
    }

    default Permission  getPermission(){
        return Permission.WRITER;
    }
}