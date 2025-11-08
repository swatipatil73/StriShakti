package com.collage.new_strishakti.data.network



import com.collage.new_strishakti.data.model.Comment.CommentModel
import com.collage.new_strishakti.data.model.Comment.CommentResponse
import com.collage.new_strishakti.data.model.FriendListResponse
import com.collage.new_strishakti.data.model.Reel.ReelResponse
import com.collage.new_strishakti.data.model.Reel.ReelUploadResponse
import com.collage.new_strishakti.data.model.friend.SearchFriendsResponse
import com.collage.new_strishakti.data.model.post.AdsResponse
import com.collage.new_strishakti.data.model.post.AnnouncementResponse
import com.collage.new_strishakti.data.model.post.CommonResponse
import com.collage.new_strishakti.data.model.post.CreatePostResponse
import com.collage.new_strishakti.data.model.post.HomeResponse
import com.collage.new_strishakti.data.model.post.ReelsResponse
import com.collage.new_strishakti.data.model.regi.College
import com.collage.new_strishakti.data.model.regi.Department
import com.collage.new_strishakti.data.model.regi.District
import com.collage.new_strishakti.data.model.regi.LoginRequest
import com.collage.new_strishakti.data.model.regi.LoginResponse
import com.collage.new_strishakti.data.model.regi.RegisterRequest
import com.collage.new_strishakti.data.model.regi.School
import com.collage.new_strishakti.data.model.regi.State
import com.collage.new_strishakti.data.model.regi.StreamItem
import com.collage.new_strishakti.data.model.regi.StudyCentre
import com.collage.new_strishakti.data.model.regi.Taluka
import com.collage.new_strishakti.data.model.regi.UniversityResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {


    @POST("ssakti/users/user/userRegister")
    suspend fun registerUser(@Body request: RegisterRequest): Response<ResponseBody>

    @GET("public/api/state/getAllStates")
    suspend fun getAllStates(): Response<List<State>>

    @GET("public/api/district/getDistrictsByStateId/{stateId}")
    suspend fun getDistricts(@Path("stateId") stateId: Int): Response<List<District>>

    @GET("public/api/taluka/talukaByDitrictId/{districtId}")
    suspend fun getTalukas(@Path("districtId") districtId: Int): Response<List<Taluka>>


    @GET("api/university/list")
    suspend fun getUniversities(): Response<UniversityResponse>




    @GET("api/college/university/id/{universityId}")
    suspend fun getCollegesByUniversity(@Path("universityId") universityId: Int): Response<List<College>>

    @GET("api/schools/university/{universityId}")
    suspend fun getSchoolsByUniversity(@Path("universityId") universityId: Int): Response<List<School>>

    @GET("api/study-centre/university/id/{universityId}")
    suspend fun getStudyCentresByUniversity(@Path("universityId") universityId: Int): Response<List<StudyCentre>>

    @GET("api/departments/getAll")
    suspend fun getAllDepartments(): Response<List<Department>>

    @GET("api/stream-updated/getAll")
    suspend fun getAllStreams(): Response<List<StreamItem>>


    @Headers("Content-Type: application/json")
    @POST("ssakti/users/user/userLogin")
    fun loginUser(@Body request: LoginRequest): Call<LoginResponse>

    //post

    // 1) Announcement
    // Example: /ssakti/users/user/getAnnouncement/SUP_ADMIN_ANNOUNCEMENT?postCategory=...
    @GET("ssakti/users/user/getAnnouncement/{postCategory}")
    suspend fun getAnnouncement(
        @Path("postCategory") postCategory: String,
        @Header("Authorization") token: String
    ): Response<AnnouncementResponse>



    // 2) Ads - SUP_ADMIN
    @GET("sskati/users/posts/getAds/SUP_ADMIN")
    suspend fun getAdsSuperAdmin(
        @Query("postCategory") postCategory: String?,
        @Header("Authorization") token: String
    ): Response<AdsResponse>

    // 3) Ads - ADMIN
    @GET("sskati/users/posts/getAds/ADMIN")
    suspend fun getAdsAdmin(
        @Query("postCategory") postCategory: String?,
        @Header("Authorization") token: String
    ): Response<AdsResponse>

    // 4) Home posts (paginated)
    // Example: /ssakti/users/home/home/v1/{userId}?cursor=0&size=5
    @GET("ssakti/users/home/home/v1/{userId}")
    suspend fun getHomePosts(
        @Path("userId") userId: Long,
        @Query("cursor") cursor: Long,
        @Query("size") size: Int,
        @Header("Authorization") token: String
    ): Response<HomeResponse>

    // 5) Reels (paginated)
    // /ssakti/users/reels/getAllReels?page=0&size=10
    @GET("ssakti/users/reels/getAllReels")
    suspend fun getAllReels(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Header("Authorization") token: String
    ): Response<ReelsResponse>


    @Multipart
    @POST("sskati/users/posts/addPost/{userId}")
    suspend fun addPost(
        @Path("userId") userId: Int,
        @Header("Authorization") token: String, // pass token dynamically
        @Part("postName") postName: RequestBody,
        @Part("postType") postType: RequestBody,
        @Part("videoThumbnailUrl") videoThumbnailUrl: RequestBody,
        @Part postImage: List<MultipartBody.Part>?
    ): Response<CreatePostResponse>

//deleet
        @DELETE("sskati/users/posts/deletPost/{postId}")
        suspend fun deletePost(
            @Path("postId") postId: Int,
            @Header("Authorization") bearer: String
        ): Response<CommentResponse>

        //save  https://dev.api.strishakti.org/ssakti/users/savepost/savePost/352/1446
        @POST("ssakti/users/savepost/savePost/{userId}/{postId}")
        suspend fun savePost(
            @Path("userId") userId: Int,
            @Path("postId") postId: Int,
            @Header("Authorization") token: String
        ): Response<CommonResponse>


        //repeot   ssakti/users/dispute/raiseDispute


    @POST("ssakti/users/dispute/raiseDispute/{userId}/{postId}/{disputeTitleId}")
    suspend fun reportPost(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int,
        @Path("postId") postId: Int,
        @Path("disputeTitleId") disputeTitleId: Int,
        @Body body: Map<String, String>
    ): Response<CommonResponse>
    //like

    // ✅ LIKE POST
    @POST("ssakti/users/postreact/addReactOnPost/{userId}/{postId}")
    suspend fun addReactOnPost(
        @Path("userId") userId: String,
        @Path("postId") postId: String,
        @Body body: RequestBody,
        @Header("Authorization") token: String
    ): Response<CommonResponse>

    // ✅ UNLIKE POST
    @DELETE("ssakti/users/postreact/deleteReactOnPost/{userId}/{postId}")
    suspend fun deleteReactOnPost(
        @Path("userId") userId: String,
        @Path("postId") postId: String,
        @Header("Authorization") token: String
    ): Response<CommonResponse>



    @GET("ssakti/users/postcomment/getCommentsAndReacts/{postId}")
    suspend fun getComments(
        @Header("Authorization") token: String,
        @Path("postId") postId: Int
    ): Response<CommentResponse>

    @POST("ssakti/users/postcomment/addCommentOnPost/{userId}/{postId}")

    suspend fun addComment(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int,
        @Path("postId") postId: Int,
        @Body body: Map<String, String>
    ): Response<CommentModel>

    @POST("ssakti/users/postcomment/addCommentOnComment/{userId}/{postId}/{commentId}")


    suspend fun addReply(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int,
        @Path("postId") postId: Int,
        @Path("commentId") commentId: Int,
        @Body body: Map<String, String>
    ): Response<CommonResponse> // or ApiResponse if that’s your model


    @DELETE("ssakti/users/postcomment/deleteParentComment/{userId}/{postCommentId}")
    suspend fun deleteParentComment(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int,
        @Path("postCommentId") postCommentId: Int
    ): Response<CommonResponse>

    //reel
    @GET("ssakti/users/reels/getAllReels")
    suspend fun getReels(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): ReelResponse


    @Multipart
    @POST("ssakti/users/reels/addReel/{userId}")
    suspend fun addReel(
        @Path("userId") userId: Int,
        @Header("Authorization") token: String,
        @Part("postName") postName: RequestBody,
        @Part("postType") postType: RequestBody,
        @Part("videoThumbnailUrl") videoThumbnailUrl: RequestBody,
        @Part postImage: List<MultipartBody.Part>?
    ): Response<ReelUploadResponse>


    @DELETE("ssakti/users/reels/deletReel/{reelId}")
    suspend fun deleteReel(
        @Path("reelId") reelId: Int,
        @Header("Authorization") token: String
    ): Response<CommonResponse>

    //FriendListResponse


    @GET("ssakti/users/friendrequest/getFriendsList/{userId}")
    suspend fun getFriendsList(
        @Path("userId") userId: Int,
        @Header("Authorization") token: String
    ): Response<FriendListResponse>

    @GET("ssakti/users/user/searchUser/{userName}")
    suspend fun searchFriends(
        @Path("userName") userName: String,
        @Header("Authorization") token: String
    ): Response<SearchFriendsResponse>

}



