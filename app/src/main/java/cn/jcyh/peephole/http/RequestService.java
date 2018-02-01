package cn.jcyh.peephole.http;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by jogger on 2018/1/10.
 */

public interface RequestService {
    String BASE_URL = "http://demo.anychat.cn";

    //    @FormUrlEncoded
//    @POST("app/signup")
//    Observable<User> regist(@Query("mobile") String uid, @Query
//            ("password") String password, @Query("name") String name, @Query("city") String city,
//                            @Query("device") String device);
    @FormUrlEncoded
    @POST("app/signup")
    Call<ResponseBody> regist(@Field("mobile") String uid, @Field
            ("password") String password, @Field("name") String name, @Field("city") String city,
                              @Field("device") String device);

    //    @FormUrlEncoded
//    @POST("app/login")
//    Observable<User> login(@Field("mobile") String uid, @Field
//            ("password") String password);
    @FormUrlEncoded
    @POST("app/login")
    Call<ResponseBody> login(@Field("mobile") String uid, @Field
            ("password") String password);
}
